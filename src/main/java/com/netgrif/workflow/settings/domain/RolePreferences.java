package com.netgrif.workflow.settings.domain;

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
 * Role application preferences. Contains:
 * <ul>
 *     <li>task filters for each task view</li>
 *     <li>case filters for each case view</li>
 * </ul>
 */
@Document
@Data
@NoArgsConstructor
public class RolePreferences implements Serializable {

    @Id
    private Long processRoleId;

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

    public RolePreferences(Long processRoleId) {
        this.processRoleId = processRoleId;
    }
}