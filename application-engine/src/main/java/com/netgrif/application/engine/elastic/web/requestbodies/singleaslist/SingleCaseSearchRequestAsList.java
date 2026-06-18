package com.netgrif.application.engine.elastic.web.requestbodies.singleaslist;

import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.workflow.utils.CaseSearchRequestSingleItemAsListDeserializer;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.annotation.JsonDeserialize;

@Slf4j
@JsonDeserialize(using = CaseSearchRequestSingleItemAsListDeserializer.class, contentAs = CaseSearchRequest.class)
public class SingleCaseSearchRequestAsList extends SingleItemAsList<CaseSearchRequest> {
}
