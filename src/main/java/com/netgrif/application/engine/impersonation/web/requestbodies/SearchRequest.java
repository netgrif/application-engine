package com.netgrif.application.engine.impersonation.web.requestbodies;

import lombok.Getter;
import lombok.Setter;

public class SearchRequest {

    @Setter
    @Getter
    protected String query;

    public SearchRequest() {
    }

    public SearchRequest(String query) {
        this.query = query;
    }
}
