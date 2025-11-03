package com.netgrif.application.engine.objects.dto.response.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;

import java.io.Serializable;

public record TransitionImportReferenceDto(PositionDto position, I18nString title, String stringId, String importId) implements Serializable {

    public static TransitionImportReferenceDto fromTransition(Transition transition) {
        return new TransitionImportReferenceDto(PositionDto.fromPosition(transition.getPosition()), transition.getTitle(),
                transition.getObjectId().toString(), transition.getImportId());
    }
}
