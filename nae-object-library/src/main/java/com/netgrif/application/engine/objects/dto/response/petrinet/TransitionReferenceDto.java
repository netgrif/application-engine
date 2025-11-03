package com.netgrif.application.engine.objects.dto.response.petrinet;

import java.io.Serializable;
import java.util.List;

public record TransitionReferenceDto(String stringId,
                                     String title,
                                     String petriNetId,
                                     List<DataFieldReferenceDto> immediateData) implements Serializable {

    public TransitionReferenceDto(String stringId, String title, String petriNetId, List<DataFieldReferenceDto> immediateData) {
        this.stringId = stringId;
        this.title = title;
        this.petriNetId = petriNetId;
        this.immediateData = immediateData;
    }
}
