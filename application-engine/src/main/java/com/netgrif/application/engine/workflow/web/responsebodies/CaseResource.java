package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.web.WorkflowController;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

@Slf4j
@Relation(collectionRelation = "cases", itemRelation = "cases")
public class CaseResource extends EntityModel<Case> {

    public CaseResource(Case content) {
        super(content, new ArrayList<>());
        log.debug("Creating CaseResource, id={}, locale={}, caseClass={}", content.get_id(), LocaleContextHolder.getLocale(), content.getClass().getName());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(WorkflowController.class)
                        .createCase(new CreateCaseBody(), null, LocaleContextHolder.getLocale()))
                .withRel("create"));
    }
}
