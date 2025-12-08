package com.netgrif.application.engine.objects.dto.response.petrinet;


import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public record PetriNetReferenceDto(String stringId, String title, String identifier, String version, String initials,
                                   String defaultCaseName, String icon, LocalDateTime creationDate, ActorRef author,
                                   List<DataFieldReferenceDto> immediateData) implements Serializable {

    public static PetriNetReferenceDto fromPetriNet(PetriNet net, Locale locale) {
        return new PetriNetReferenceDto(net.getStringId(), net.getTranslatedTitle(locale), net.getIdentifier(),
                net.getVersion().toString(), net.getInitials(), net.getTranslatedDefaultCaseName(locale), net.getIcon(),
                net.getCreationDate(), net.getAuthor(),
                net.getImmediateFields().stream().map(field -> DataFieldReferenceDto.fromField(field, locale)).toList()
        );
    }
}
