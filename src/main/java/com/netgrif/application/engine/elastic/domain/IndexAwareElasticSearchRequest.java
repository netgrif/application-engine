package com.netgrif.application.engine.elastic.domain;


import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class IndexAwareElasticSearchRequest extends ArrayList<CaseSearchRequest> implements List<CaseSearchRequest> {

    public static final String QUERY_ALL_INDEXES = "_all_";

    /**
     * taskIds of task "view" in preference_menu_item (by default part of URL)
     */
    @Getter
    @Setter
    private List<String> menuItemIds;

    /**
     * actual index name in elasticsearch
     */
    @Getter
    @Setter
    private List<String> indexNames;

    private IndexAwareElasticSearchRequest(List<String> menuItemIds, List<String> indexNames) {
        this.menuItemIds = menuItemIds;
        this.indexNames = indexNames;
    }

    public static IndexAwareElasticSearchRequest all() {
        return new IndexAwareElasticSearchRequest(null, Collections.singletonList(QUERY_ALL_INDEXES));
    }

    public static IndexAwareElasticSearchRequest ofIndex(String indexName) {
        return new IndexAwareElasticSearchRequest(null, Collections.singletonList(indexName));
    }

    public static IndexAwareElasticSearchRequest ofIndexes(List<String> indexNames) {
        return new IndexAwareElasticSearchRequest(null, Collections.unmodifiableList(indexNames));
    }

    public static IndexAwareElasticSearchRequest ofMenuItems(List<String> menuItemIds) {
        return new IndexAwareElasticSearchRequest(Collections.unmodifiableList(menuItemIds), null);
    }

    public boolean doQueryAll() {
        return this.indexNames != null && !this.indexNames.isEmpty() && this.indexNames.get(0).equals(QUERY_ALL_INDEXES);
    }
}
