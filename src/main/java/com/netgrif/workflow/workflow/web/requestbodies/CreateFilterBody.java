package com.netgrif.workflow.workflow.web.requestbodies;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
