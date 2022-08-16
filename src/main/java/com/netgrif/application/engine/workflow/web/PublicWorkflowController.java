package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Slf4j
@RestController
@ConditionalOnProperty(
        value = "nae.public.workflow.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequestMapping({"/api/public"})
@Tag(name = "Public Workflow Controller")
public class PublicWorkflowController {

    private final IWorkflowService workflowService;

    private final IUserService userService;

    public PublicWorkflowController(IWorkflowService workflowService, IUserService userService) {
        this.userService = userService;
        this.workflowService = workflowService;
    }

    @PreAuthorize("@workflowAuthorizationService.canCallCreate(@userService.getAnonymousLogged(), #body.netId)")
    @PostMapping(value = "/case", consumes = "application/json;charset=UTF-8", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Create new case")
    public EntityModel<EventOutcomeWithMessage> createCase(@RequestBody CreateCaseBody body, Locale locale) {
        LoggedUser loggedUser = userService.getAnonymousLogged();
        try {
            CreateCaseEventOutcome outcome = this.workflowService.createCase(body.netId, body.title, body.color, loggedUser, locale);
            return EventOutcomeWithMessageResource.successMessage("Case created successfully",
                    LocalisedEventOutcomeFactory.from(outcome, locale));
        } catch (Exception e) {
            log.error("Creating case failed:" + e.getMessage(), e);
            return EventOutcomeWithMessageResource.errorMessage("Creating case failed: " + e.getMessage());
        }
    }
}
