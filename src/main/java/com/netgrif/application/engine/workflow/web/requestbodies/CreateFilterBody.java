package com.netgrif.application.engine.workflow.web.requestbodies;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilterBody {
    private String title;
    private int visibility;
    private String description;
    private String type;
    private String query;
}
