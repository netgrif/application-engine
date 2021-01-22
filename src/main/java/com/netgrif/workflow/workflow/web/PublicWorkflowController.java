package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

@RestController
@RequestMapping({"/api/public"})
@Slf4j
public class PublicWorkflowController extends PublicAbstractController {

    private final IWorkflowService workflowService;

    public PublicWorkflowController(IWorkflowService workflowService, IUserService userService) {
        super(userService);
        this.workflowService = workflowService;
    }

    @PostMapping(value = "/case", consumes = "application/json;charset=UTF-8", produces = "application/hal+json")
    @ApiOperation(value = "Create new case")
    public CaseResource createCase(@RequestBody CreateCaseBody body) {
        LoggedUser loggedUser = getAnonymous();
        try {
            Case useCase = this.workflowService.createCase(body.netId, body.title, body.color, loggedUser);
            return new CaseResource(useCase);
        } catch (Exception e) {
            log.error("Creating case failed:" + e.getMessage(), e);
            return null;
        }
    }

    @GetMapping(value = "/case/{id}/data", produces = "application/hal+json")
    @ApiOperation(value = "Get all case data")
    public DataFieldsResource getAllCaseData(@PathVariable("id") String caseId, Locale locale) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            return new DataFieldsResource(this.workflowService.getData(caseId), locale);
        } catch (UnsupportedEncodingException e) {
            log.error("Getting all case data of [" + caseId + "] failed:" + e.getMessage(), e);
            return new DataFieldsResource(new ArrayList<>(), locale);
        }
    }
}
