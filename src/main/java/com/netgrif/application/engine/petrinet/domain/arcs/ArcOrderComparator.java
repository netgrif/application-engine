package com.netgrif.application.engine.petrinet.domain.arcs;

import java.util.Comparator;

public class ArcOrderComparator implements Comparator<Arc> {

    private static final ArcOrderComparator ourInstance = new ArcOrderComparator();

    public static ArcOrderComparator getInstance() {
        return ourInstance;
    }

    private ArcOrderComparator() {
    }

    @Override
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
