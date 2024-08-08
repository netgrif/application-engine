package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.ArcType;
import com.netgrif.application.engine.importer.model.Expression;
import com.netgrif.application.engine.petrinet.domain.arcs.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public final class ArcImporter {

    private final Map<ArcType, Supplier<Arc>> arcFactory;

    public ArcImporter() {
        arcFactory = new HashMap<>();
        arcFactory.put(ArcType.REGULAR, Arc::new);
        arcFactory.put(ArcType.RESET, ResetArc::new);
        arcFactory.put(ArcType.INHIBITOR, InhibitorArc::new);
        arcFactory.put(ArcType.READ, ReadArc::new);
    }

    public Arc getArc(com.netgrif.application.engine.importer.model.Arc importArc, Importer importer) throws IllegalArgumentException {
        Arc arc = arcFactory.get(importArc.getType()).get();
        arc.setImportId(importArc.getId());
        arc.setSource(importer.getNode(importArc.getSourceId()));
        arc.setDestination(importer.getNode(importArc.getDestinationId()));
        arc.setMultiplicity(createMultiplicity(importArc.getMultiplicity()));
        importer.createProperties(importArc.getProperties(), arc.getProperties());
        return arc;
    }

    private Multiplicity createMultiplicity(Expression multiplicity) {
        if (multiplicity.isDynamic()) {
            return new Multiplicity(multiplicity.getValue());
        }
        return new Multiplicity(Integer.parseInt(multiplicity.getValue()));
    }
}