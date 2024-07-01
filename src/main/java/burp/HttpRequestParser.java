package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import db.ParameterHelperClass;
import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import utils.Logger;
import utils.StringUtils;
import utils.URLExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class HttpRequestParser {
    private MontoyaApi api;
    public HttpRequestParser(MontoyaApi api){
        this.api = api;
    }

    public HttpRequest parse(burp.api.montoya.http.message.requests.HttpRequest request){
        var method = request.method();
        var url = request.url();

        List<ParsedHttpParameter> params = request.parameters();

        if(!params.isEmpty()){
            try{
                Collection<ParameterHelperClass> parsedParameterHelper = sortParameters(params, stringToURL(url));
                return new HttpRequest(method, stringToURL(url), parsedParameterHelper);
            }
            catch(Exception ex){
                Logger.getInstance().logToError("[HttpRequestParser] Exception: " + ex.getMessage());
                return null;
            }
        }
        else{
            return new HttpRequest(method, stringToURL(url), new Vector<ParameterHelperClass>());
        }
    }


    private Collection<ParameterHelperClass> sortParameters(List<ParsedHttpParameter> params, URL url){
        var parameters2 = new Vector<ParameterHelperClass>();
        for (var p: params) {
            var burpType = p.type();
            ParameterType inferredType;

            //Map burp param types to my own types
            switch(burpType){
                case BODY:
                    inferredType = ParameterType.BODY;
                    break;
                case URL:
                    inferredType = ParameterType.URL;
                    break;
                case COOKIE:
                    inferredType = ParameterType.COOKIE;
                    break;
                case JSON:
                    inferredType = ParameterType.JSON;
                    break;
                default:
                    inferredType = ParameterType.OTHER;
            }

            var value = p.value();

            //TODO Good idea to always url decode
            if(!StringUtils.isNullOrEmpty(value) &&( inferredType == ParameterType.URL || inferredType == ParameterType.BODY))
                value = this.api.utilities().urlUtils().decode(value);

            parameters2.add(new ParameterHelperClass(p.name(), inferredType, url.getHost(), URLExtension.urlToString(url), value));
        }
        return parameters2;
    }

    private URL stringToURL(String urlString) {
        try {
            return Urls.create(urlString, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS);
        } catch (MalformedURLException e) {
            Logger.getInstance().logToError("[HttpRequestParser] invalid URL String was given to create URL object");
        }
        return null;
    }


}
