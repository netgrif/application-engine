package com.netgrif.application.engine.objects.dto.response.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.InhibitorArc;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ReadArc;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ResetArc;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public record ArcImportReferenceDto(String importId, String sourceId, String destinationId, Integer multiplicity,
                                    List<PositionDto> breakpoints, String type) implements Serializable {

    public static ArcImportReferenceDto fromArc(Arc arc) {
        return new ArcImportReferenceDto(arc.getImportId(), arc.getSourceId(), arc.getDestinationId(),
                arc.getMultiplicity(), arc.getBreakpoints().stream().map(PositionDto::fromPosition).toList(),
                resolveArcType(arc));

    }

    private static String resolveArcType(Arc arc) {
        if (arc instanceof ReadArc) {
            return "read";
        } else if (arc instanceof InhibitorArc) {
            return "inhibitor";
        } else if (arc instanceof ResetArc) {
            return "reset";
        }
        return "arc";
    }
}
