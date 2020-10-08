package com.netgrif.workflow.workflow.web.requestbodies;


import java.util.List;

public class TaskSearchBody {

    public static final int SEARCH_TIER_1 = 1;
    public static final int SEARCH_TIER_2 = 2;
    public static final int SEARCH_TIER_3 = 3;

    public int searchTier;
    public List<SearchPetriNet> petriNets;

    public TaskSearchBody() {}
}
