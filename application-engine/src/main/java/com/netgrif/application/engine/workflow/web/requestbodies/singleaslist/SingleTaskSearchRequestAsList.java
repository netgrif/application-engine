package com.netgrif.application.engine.workflow.web.requestbodies.singleaslist;

import tools.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.workflow.utils.TaskSearchRequestSingleItemAsListDeserializer;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;

@JsonDeserialize(using = TaskSearchRequestSingleItemAsListDeserializer.class, contentAs = TaskSearchRequest.class)
public class SingleTaskSearchRequestAsList extends SingleItemAsList<TaskSearchRequest> {
}
