package audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import audit.AuditFinding.FindingSeverity;
import db.MatchHelperClass;
import db.entities.ParameterMatch;
import gui.AuditFindingView;

public class CrossSessionAudit {
    
    private AuditFindingView auditFindingView;

    public CrossSessionAudit(AuditFindingView auditFindingView){
        this.auditFindingView = auditFindingView;
    }

    public void performAudit(ParameterMatch match, String sessionName, MatchHelperClass matchHelper){
        var inputValueSession = match.getInputValue().getSession();

        if(sessionName == null || "not set".equals(sessionName)
           || inputValueSession == null || "not set".equals(inputValueSession)
           || inputValueSession.equals(sessionName) ){
            return;
        }

        AuditFinding finding = buildAuditFinding(matchHelper.getInputParameterObj().getName(), inputValueSession, sessionName);
        this.auditFindingView.addFinding(finding);
    }

    public void identifyAudits(String paramName, String sessionEntered, String sessionMatched) {
        AuditFinding finding = buildAuditFinding(paramName, sessionEntered, sessionMatched);
        this.auditFindingView.addFinding(finding);
    }

    private AuditFinding buildAuditFinding(String paramName, String sessionEntered, String sessionMatched){
        return new CrossSessionAuditFinding(paramName, sessionEntered, sessionMatched);
    }

    public void sessionRename(String oldName, String newName){
        var allFindings = this.auditFindingView.getAuditFindings();
        var newFindings = new Vector<AuditFinding>();
        for(var finding : allFindings){
            if(!finding.getClass().getName().equals(CrossSessionAuditFinding.class.getName())){
                //No rename if not CrossSessionAuditFinding
                newFindings.add(finding);
                continue;
            }
            var crossSessionFinding = (CrossSessionAuditFinding)finding;
            crossSessionFinding.renameSession(oldName, newName);
            newFindings.add(crossSessionFinding);
        }
        this.auditFindingView.setAuditFindings(newFindings);
    }
    public void renderFindings() {
        this.auditFindingView.renderFindings();
    }

}
