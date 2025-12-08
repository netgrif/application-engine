package com.netgrif.application.engine.objects.dto.response.petrinet;

import java.io.Serializable;
import java.util.List;

public record PetriNetImportReferenceDto(List<TransitionImportReferenceDto> transitions,
                                         List<PlaceImportReferenceDto> places,
                                         List<ArcImportReferenceDto> arcs,
                                         List<String> assignedTasks,
                                         List<String> finishedTasks) implements Serializable {
}
