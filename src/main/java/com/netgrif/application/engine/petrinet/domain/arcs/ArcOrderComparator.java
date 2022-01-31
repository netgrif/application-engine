package com.netgrif.application.engine.petrinet.domain.arcs;

public class ArcOrderComparator {

    private static ArcOrderComparator ourInstance = new ArcOrderComparator();

    public static ArcOrderComparator getInstance() {
        return ourInstance;
    }

    private ArcOrderComparator() {
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
