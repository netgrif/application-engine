package com.netgrif.application.engine.workflow.utils;

import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SearchRequestSingleItemAsListDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void caseSearchAcceptsSingleObjectAndSanitizesFullText() throws Exception {
        SingleCaseSearchRequestAsList wrapper = mapper.readValue(
                "{\"fullText\":\"field: value\"}",
                SingleCaseSearchRequestAsList.class
        );

        assertEquals(1, wrapper.getList().size());
        assertEquals("field\\:\\ value", wrapper.getList().get(0).fullText);
    }

    @Test
    void caseSearchAcceptsArrayAndKeepsNullFullText() throws Exception {
        SingleCaseSearchRequestAsList wrapper = mapper.readValue(
                "[{\"fullText\":\"title:test\"},{\"query\":\"status:active\"}]",
                SingleCaseSearchRequestAsList.class
        );

        assertEquals(2, wrapper.getList().size());
        assertEquals("title\\:test", wrapper.getList().get(0).fullText);
        assertNull(wrapper.getList().get(1).fullText);
    }

    @Test
    void taskSearchAcceptsSingleObjectAndSanitizesFullText() throws Exception {
        SingleTaskSearchRequestAsList wrapper = mapper.readValue(
                "{\"fullText\":\"task + title\"}",
                SingleTaskSearchRequestAsList.class
        );

        assertEquals(1, wrapper.getList().size());
        assertEquals("task\\ \\+\\ title", wrapper.getList().get(0).fullText);
    }

    @Test
    void elasticTaskSearchUsesElasticWrapperAndSanitizesFullText() throws Exception {
        SingleElasticTaskSearchRequestAsList wrapper = mapper.readValue(
                "{\"fullText\":\"path/to/task\",\"query\":\"status:open\"}",
                SingleElasticTaskSearchRequestAsList.class
        );

        assertEquals(1, wrapper.getList().size());
        ElasticTaskSearchRequest request = wrapper.getList().get(0);
        assertEquals("path\\/to\\/task", request.fullText);
        assertEquals("status:open", request.query);
    }

    @Test
    void caseSearchArrayCanContainNullItemsAndStillSanitizesRealRequests() throws Exception {
        SingleCaseSearchRequestAsList wrapper = mapper.readValue(
                "[null,{\"fullText\":\"case/title\"}]",
                SingleCaseSearchRequestAsList.class
        );

        assertEquals(2, wrapper.getList().size());
        assertNull(wrapper.getList().get(0));
        assertEquals("case\\/title", wrapper.getList().get(1).fullText);
    }

    @Test
    void taskSearchArrayCanContainNullItemsAndStillSanitizesRealRequests() throws Exception {
        SingleTaskSearchRequestAsList wrapper = mapper.readValue(
                "[null,{\"fullText\":\"task:title\"}]",
                SingleTaskSearchRequestAsList.class
        );

        assertEquals(2, wrapper.getList().size());
        assertNull(wrapper.getList().get(0));
        assertEquals("task\\:title", wrapper.getList().get(1).fullText);
    }
}
