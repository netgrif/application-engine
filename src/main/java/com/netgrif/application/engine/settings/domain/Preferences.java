package com.netgrif.application.engine.settings.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Identity application preferences. Contains:
 * <ul>
 *     <li>locale</li>
 *     <li>task filters for each task view</li>
 *     <li>case filters for each case view</li>
 *     <li>case view flex fields</li>
 * </ul>
 */
@Document
@Data
@NoArgsConstructor
public class Preferences implements Serializable {

    @Id
    private String identityId;

    private String locale;

    private int drawerWidth;

    /**
     * taskViewId: [filterIds]
     */
    @Field
    private Map<String, List<String>> taskFilters = new HashMap<>();

    /**
     * caseViewId: [filterIds]
     */
    @Field
    private Map<String, List<String>> caseFilters = new HashMap<>();

    /**
     * caseViewId: [headersIds]
     */
    @Field
    private Map<String, List<String>> headers = new HashMap<>();

    public Preferences(String identityId) {
        this.identityId = identityId;
        this.drawerWidth = 200;
    }
}