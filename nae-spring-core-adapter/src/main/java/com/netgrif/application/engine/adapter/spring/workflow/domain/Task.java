package com.netgrif.application.engine.adapter.spring.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.objects.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.objects.workflow.domain.triggers.Trigger;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
@QueryEntity
public class Task extends com.netgrif.application.engine.objects.workflow.domain.Task {

    public Task() {
        super();
    }

    @Builder(builderMethodName = "with")
    public Task(ProcessResourceId _id, String processId, String caseId, String transitionId, TaskLayout layout, I18nString title,
                String caseColor, String caseTitle, Integer priority, ActorRef assignee, AbstractUser user,
                List<Trigger> triggers, Map<String, Map<String, Boolean>> roles, Map<String, Map<String, Boolean>> actorRefs,
                Map<String, Map<String, Boolean>> actors, List<String> viewRoles, List<String> viewActorRefs, List<String> viewActors,
                List<String> negativeViewRoles, List<String> negativeViewActors, LocalDateTime startDate, LocalDateTime finishDate,
                String finishedBy, String transactionId, Boolean requiredFilled, LinkedHashSet<String> immediateDataFields, List<Field<?>> immediateData,
                String icon, AssignPolicy assignPolicy, DataFocusPolicy dataFocusPolicy, FinishPolicy finishPolicy, Map<EventType, I18nString> eventTitles,
                Map<String, Boolean> assignedUserPolicy, Map<String, Integer> consumedTokens, Map<String, String> tags) {
        super(_id, processId, caseId, transitionId, layout, title, caseColor, caseTitle, priority, assignee, user,
                triggers, roles, actorRefs, actors, viewRoles, viewActorRefs, viewActors, negativeViewRoles, negativeViewActors,
                startDate, finishDate, finishedBy, transactionId, requiredFilled, immediateDataFields, immediateData, icon,
                assignPolicy, dataFocusPolicy, finishPolicy, eventTitles, assignedUserPolicy, consumedTokens, tags);
    }

    @Id
    @Override
    public ProcessResourceId get_id() {
        return super.get_id();
    }

    @Transient
    @Override
    public List<Field<?>> getImmediateData() {
        return super.getImmediateData();
    }

    @Transient
    @Override
    public AbstractUser getUser() {
        return super.getUser();
    }

    @JsonIgnore
    @Override
    public I18nString getTitle() {
        return super.getTitle();
    }
}
