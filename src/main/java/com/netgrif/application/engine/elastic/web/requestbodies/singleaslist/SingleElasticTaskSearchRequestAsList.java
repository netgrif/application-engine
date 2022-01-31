package com.netgrif.application.engine.elastic.web.requestbodies.singleaslist;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;

@JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = ElasticTaskSearchRequest.class)
public class SingleElasticTaskSearchRequestAsList extends SingleItemAsList<ElasticTaskSearchRequest> {
}