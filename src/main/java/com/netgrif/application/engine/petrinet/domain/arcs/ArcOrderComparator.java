package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.core.petrinet.domain.arcs.Arc;
import com.netgrif.core.petrinet.domain.arcs.ResetArc;

public class ArcOrderComparator {

    private static ArcOrderComparator ourInstance = new ArcOrderComparator();

    private ArcOrderComparator() {
    }

    public static ArcOrderComparator getInstance() {
        return ourInstance;
    }

    public int compare(Arc first, Arc second) {
        if (first instanceof ResetArc) {
            return 1;
        }
        if (second instanceof ResetArc) {
            return -1;
        }
        return 0;
    }
}
