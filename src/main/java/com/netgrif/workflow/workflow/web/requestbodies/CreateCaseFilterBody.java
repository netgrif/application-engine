package com.netgrif.workflow.workflow.web.requestbodies;

import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;

public class CreateCaseFilterBody extends CreateFilterBody<SingleCaseSearchRequestAsList> {
    public CreateCaseFilterBody(String title, int visibility, String description, String type, SingleCaseSearchRequestAsList query) {
        super(title, visibility, description, type, query);
    }
}