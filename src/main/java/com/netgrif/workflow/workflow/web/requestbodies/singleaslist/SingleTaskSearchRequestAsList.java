package com.netgrif.workflow.workflow.web.requestbodies.singleaslist;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.workflow.utils.SingleItemAsList;
import com.netgrif.workflow.utils.SingleItemAsListDeserializer;
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest;

@JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = TaskSearchRequest.class)
public class SingleTaskSearchRequestAsList extends SingleItemAsList<TaskSearchRequest> {
}