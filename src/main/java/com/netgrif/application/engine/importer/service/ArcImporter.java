package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.ArcType;
import com.netgrif.application.engine.importer.model.Expression;
import com.netgrif.application.engine.petrinet.domain.Node;
import com.netgrif.application.engine.petrinet.domain.arcs.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public final class ArcImporter {

    private final Map<ArcType, Supplier<PTArc>> ptArcFactory;
    private final Map<ArcType, Supplier<TPArc>> tpArcFactory;

    public ArcImporter() {
        ptArcFactory = new HashMap<>();
        ptArcFactory.put(ArcType.REGULAR, RegularPTArc::new);
        ptArcFactory.put(ArcType.RESET, ResetArc::new);
        ptArcFactory.put(ArcType.INHIBITOR, InhibitorArc::new);
        ptArcFactory.put(ArcType.READ, ReadArc::new);
        tpArcFactory = new HashMap<>();
        tpArcFactory.put(ArcType.REGULAR, RegularTPArc::new);
    }

    public Arc<? extends Node, ? extends Node> getArc(com.netgrif.application.engine.importer.model.Arc importArc, Importer importer) {
        Arc<? extends Node, ? extends Node> arc;
        if (importer.isInputArc(importArc)) {
            arc = getInputArc(importArc, importer);
        } else {
            arc = getOutputArc(importArc, importer);
        }
        arc.setImportId(importArc.getId());
        arc.setMultiplicityExpression(createMultiplicity(importArc.getMultiplicity()));
        importer.createProperties(importArc.getProperties(), arc.getProperties());
        return arc;
    }

    public TPArc getOutputArc(com.netgrif.application.engine.importer.model.Arc importArc, Importer importer) throws IllegalArgumentException {
        TPArc arc = tpArcFactory.get(importArc.getType()).get();
        arc.setSource(importer.getTransition(importArc.getSourceId()));
        arc.setDestination(importer.getPlace(importArc.getDestinationId()));
        return arc;
    }

    public PTArc getInputArc(com.netgrif.application.engine.importer.model.Arc importArc, Importer importer) throws IllegalArgumentException {
        PTArc arc = ptArcFactory.get(importArc.getType()).get();
        arc.setSource(importer.getPlace(importArc.getSourceId()));
        arc.setDestination(importer.getTransition(importArc.getDestinationId()));
        return arc;
    }

    private Multiplicity createMultiplicity(Expression multiplicity) {
        if (multiplicity.isDynamic()) {
            return new Multiplicity(multiplicity.getValue());
        }
        return new Multiplicity(Integer.parseInt(multiplicity.getValue()));
    }
}