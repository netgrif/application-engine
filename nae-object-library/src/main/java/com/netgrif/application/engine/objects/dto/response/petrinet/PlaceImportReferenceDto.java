package com.netgrif.application.engine.objects.dto.response.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.Place;

import java.io.Serializable;

public record PlaceImportReferenceDto(String setImportId, I18nString title, Integer tokens, PositionDto position,
                                      Boolean isStatic) implements Serializable {

    public static PlaceImportReferenceDto fromPlace(Place place) {
        return new PlaceImportReferenceDto(place.getImportId(), place.getTitle(), place.getTokens(),
                PositionDto.fromPosition(place.getPosition()), place.getIsStatic());
    }
}
