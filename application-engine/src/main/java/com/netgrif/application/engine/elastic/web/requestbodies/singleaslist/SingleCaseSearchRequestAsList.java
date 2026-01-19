package com.netgrif.application.engine.elastic.web.requestbodies.singleaslist;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.workflow.utils.CaseSearchRequestSingleItemAsListDeserializer;

@JsonDeserialize(using = CaseSearchRequestSingleItemAsListDeserializer.class, contentAs = CaseSearchRequest.class)
public class SingleCaseSearchRequestAsList extends SingleItemAsList<CaseSearchRequest> {
}
