package com.netgrif.application.engine.workflow.domain.arcs;

public class ArcOrderComparator {

    private static final ArcOrderComparator ourInstance = new ArcOrderComparator();

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
