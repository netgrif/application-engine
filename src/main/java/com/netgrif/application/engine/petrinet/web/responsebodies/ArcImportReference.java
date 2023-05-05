package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.PetriNetObject;
import com.netgrif.application.engine.petrinet.domain.Position;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.InhibitorArc;
import com.netgrif.application.engine.petrinet.domain.arcs.ReadArc;
import com.netgrif.application.engine.petrinet.domain.arcs.ResetArc;
import lombok.Data;

import java.util.List;

@Data
public class ArcImportReference extends PetriNetObject {

    protected String sourceId;

    protected String destinationId;

    protected Integer multiplicity;

    protected List<Position> breakpoints;

    protected String type;

    public ArcImportReference(Arc arc) {
        this.setObjectId(arc.getObjectId());
        this.setImportId(arc.getImportId());
        this.sourceId = arc.getSourceId();
        this.destinationId = arc.getDestinationId();
        this.multiplicity = arc.getMultiplicity();
        this.breakpoints = arc.getBreakpoints();
        this.type = type(arc);
    }

    private String type(Arc arc) {
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
