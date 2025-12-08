package com.netgrif.application.engine.objects.dto.response.workflow;

import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.localised.LocalisedField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.localised.LocalisedFieldFactory;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.TaskPair;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * DTO for {@link Case}
 */
public record CaseDto(String stringId, String processIdentifier, String title, String icon, ActorRef author,
                      String visualId, ObjectId petriNetObjectId, LocalDateTime lastModified,
                      LocalDateTime creationDate,
                      String color, Map<String, Integer> activePlaces, List<LocalisedField> immediateData,
                      Map<String, Integer> consumedTokens, Set<TaskPair> tasks, Set<String> enabledRoles,
                      Map<String, Map<String, Boolean>> permissions, Map<String, Map<String, Boolean>> userRefs,
                      Map<String, Map<String, Boolean>> users, List<String> viewRoles, List<String> viewUserRefs,
                      List<String> viewUsers, List<String> negativeViewRoles, List<String> negativeViewUsers,
                      Map<String, String> tags) implements Serializable {

    public static CaseDto fromCase(Case aCase, Locale locale) {
        return new CaseDto(aCase.getStringId(), aCase.getProcessIdentifier(), aCase.getTitle(), aCase.getIcon(),
                aCase.getAuthor(), aCase.getVisualId(), aCase.getPetriNetObjectId(), aCase.getLastModified(),
                aCase.getCreationDate(), aCase.getColor(), aCase.getActivePlaces(),
                aCase.getImmediateData().stream().map(field -> LocalisedFieldFactory.from(field, locale)).toList(),
                aCase.getConsumedTokens(), aCase.getTasks(), aCase.getEnabledRoles(), aCase.getPermissions(),
                aCase.getUserRefs(), aCase.getUsers(), aCase.getViewRoles(), aCase.getViewUserRefs(), aCase.getViewUsers(),
                aCase.getNegativeViewRoles(), aCase.getNegativeViewUsers(), aCase.getTags()
        );
    }
}