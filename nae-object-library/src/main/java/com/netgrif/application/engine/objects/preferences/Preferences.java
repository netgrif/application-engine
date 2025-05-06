package com.netgrif.application.engine.objects.preferences;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User application preferences. Contains:
 * <ul>
 *     <li>locale</li>
 *     <li>task filters for each task view</li>
 *     <li>case filters for each case view</li>
 *     <li>case view flex fields</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public abstract class Preferences implements Serializable {

    private String userId;

    private String locale;

    private int drawerWidth;

    /**
     * taskViewId: [filterIds]
     */
    private Map<String, List<String>> taskFilters = new HashMap<>();

    /**
     * caseViewId: [filterIds]
     */
    private Map<String, List<String>> caseFilters = new HashMap<>();

    /**
     * caseViewId: [headersIds]
     */
    private Map<String, List<String>> headers = new HashMap<>();

    public Preferences(String userId) {
        this.userId = userId;
        this.drawerWidth = 200;
    }
}