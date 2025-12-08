package com.netgrif.application.engine.objects.dto.response.task;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.localised.LocalisedField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.localised.LocalisedFieldFactory;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Localised task data object
 */
public record TaskDto(String stringId, String caseId, String transitionId, String title, String caseTitle,
                      Integer priority,
                      @Nullable String userId, @Nullable String userRealmId, @Nullable TaskLayout layout,
                      @Nullable String caseColor,
                      Map<String, Map<String, Boolean>> roles, @Nullable Map<String, Map<String, Boolean>> users,
                      LocalDateTime startDate, @Nullable LocalDateTime finishDate, @Nullable String finishedBy,
                      @Nullable String transactionId,
                      Boolean requiredFilled, @Nullable List<LocalisedField> immediateData, @Nullable String icon,
                      String assignPolicy, @Nullable String dataFocusPolicy, @Nullable String finishPolicy,
                      @Nullable String finishTitle,
                      String cancelTitle, @Nullable String delegateTitle, @Nullable String assignTitle,
                      Map<String, Boolean> assignedUserPolicy, @Nullable Map<String, String> tags) {

    public static TaskDto fromTask(Task task, Locale locale) {
        return new TaskDto(
                task.getStringId(),
                task.getCaseId(),
                task.getTransitionId(),
                task.getTitle().getTranslation(locale),
                task.getCaseTitle(),
                task.getPriority(),
                task.getUser() != null ? task.getUser().getStringId() : null,
                task.getUser() != null ? task.getUser().getRealmId() : null,
                task.getLayout(),
                task.getCaseColor(),
                task.getRoles(),
                task.getUsers(),
                task.getStartDate(),
                task.getFinishDate(),
                task.getFinishedBy(),
                task.getTransactionId(),
                task.getRequiredFilled(),
                task.getImmediateData().stream().map(field -> LocalisedFieldFactory.from(field, locale)).toList(),
                task.getIcon(),
                task.getAssignPolicy().toString(),
                task.getDataFocusPolicy().toString(),
                task.getFinishPolicy().toString(),
                task.getTranslatedEventTitle(EventType.FINISH, locale),
                task.getTranslatedEventTitle(EventType.ASSIGN, locale),
                task.getTranslatedEventTitle(EventType.CANCEL, locale),
                task.getTranslatedEventTitle(EventType.DELEGATE, locale),
                task.getAssignedUserPolicy(),
                task.getTags());
    }
}
