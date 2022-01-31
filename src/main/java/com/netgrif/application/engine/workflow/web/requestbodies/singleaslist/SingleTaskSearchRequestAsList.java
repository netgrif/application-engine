package com.netgrif.application.engine.workflow.web.requestbodies.singleaslist;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;

@JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = TaskSearchRequest.class)
public class SingleTaskSearchRequestAsList extends SingleItemAsList<TaskSearchRequest> {
}