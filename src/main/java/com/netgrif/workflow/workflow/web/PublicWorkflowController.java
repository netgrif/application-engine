package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

@RestController
@RequestMapping({"/api/public"})
@Slf4j
public class PublicWorkflowController {

    private final IWorkflowService workflowService;

    private final IUserService userService;

    public PublicWorkflowController(IWorkflowService workflowService, IUserService userService) {
        this.userService = userService;
        this.workflowService = workflowService;
    }

    @PostMapping(value = "/case", consumes = "application/json;charset=UTF-8", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiOperation(value = "Create new case")
    public EventOutcomeWithMessageResource createCase(@RequestBody CreateCaseBody body, Locale locale) {
        LoggedUser loggedUser = userService.getAnonymousLogged();
        try {
            CreateCaseEventOutcome outcome = this.workflowService.createCase(body.netId, body.title, body.color, loggedUser, locale);
            return EventOutcomeWithMessageResource.successMessage("Case created succesfully", outcome);
        } catch (Exception e) {
            log.error("Creating case failed:" + e.getMessage(), e);
            return EventOutcomeWithMessageResource.errorMessage("Creating case failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/case/{id}/data", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiOperation(value = "Get all case data")
    public EventOutcomeWithMessageResource getAllCaseData(@PathVariable("id") String caseId, Locale locale) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            return EventOutcomeWithMessageResource.successMessage("Getting all data of [" + caseId + "] succeeded",this.workflowService.getData(caseId));
        } catch (UnsupportedEncodingException e) {
            log.error("Getting all case data of [" + caseId + "] failed:" + e.getMessage(), e);
            return EventOutcomeWithMessageResource.errorMessage("Getting all case data of [" + caseId + "] failed:" + e.getMessage());
        }
    }
}
