package com.netgrif.application.engine.elastic.domain;


import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@NoArgsConstructor
public class IndexAwareElasticSearchRequest extends ArrayList<CaseSearchRequest> implements List<CaseSearchRequest> {

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

    @Getter
    @Setter
    private boolean allIndex;

    private IndexAwareElasticSearchRequest(List<String> menuItemIds, List<String> indexNames, Boolean allIndex) {
        this.menuItemIds = menuItemIds;
        this.indexNames = indexNames;
        this.allIndex = Objects.requireNonNullElse(allIndex, false);
    }

    public static IndexAwareElasticSearchRequest all() {
        return new IndexAwareElasticSearchRequest(null, null, true);
    }

    public static IndexAwareElasticSearchRequest ofIndex(String indexName) {
        return new IndexAwareElasticSearchRequest(null, Collections.singletonList(indexName), false);
    }

    public static IndexAwareElasticSearchRequest ofIndexes(List<String> indexNames) {
        return new IndexAwareElasticSearchRequest(null, Collections.unmodifiableList(indexNames), false);
    }

    public static IndexAwareElasticSearchRequest ofMenuItems(List<String> menuItemIds) {
        return new IndexAwareElasticSearchRequest(Collections.unmodifiableList(Optional.ofNullable(menuItemIds).orElse(Collections.emptyList())), null, false);
    }

    public boolean doQueryAll() {
        return this.allIndex;
    }
}
