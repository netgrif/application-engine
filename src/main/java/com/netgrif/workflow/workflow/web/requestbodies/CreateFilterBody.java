package com.netgrif.workflow.workflow.web.requestbodies;


import lombok.Data;

@Data
public class CreateFilterBody {

    private String title;
    private int visibility;
    private String description;
    private String type;
    private String query;

    public CreateFilterBody() {
    }

    public CreateFilterBody(String title, int visibility, String description, String type, String query) {
        this.title = title;
        this.visibility = visibility;
        this.description = description;
        this.type = type;
        this.query = query;
    }
}
