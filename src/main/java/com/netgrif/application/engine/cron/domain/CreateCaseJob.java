package com.netgrif.application.engine.cron.domain;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateCaseJob implements Job {

    public static final String CASE_ID = "caseId";

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    @Override
    public void execute(JobExecutionContext context) {
        String caseId = getInstanceId(context);
        Case cronJobCase = workflowService.findOne(caseId);
        String processId = cronJobCase.getFieldValue("process_id_map").toString();
        workflowService.createCaseByIdentifier(processId, "", "", userService.getSystem().transformToLoggedUser());
    }

    public String getInstanceId(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(CASE_ID);
    }
}
