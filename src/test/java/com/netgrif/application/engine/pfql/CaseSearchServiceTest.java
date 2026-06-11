package com.netgrif.application.engine.pfql;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.caseresource.CaseSearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class CaseSearchServiceTest {

    private static final String QUERY_FIELD_ID = "input_query";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private CaseSearchService caseSearchService;

    @Autowired
    private ImportHelper importHelper;

    private Case testCase;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        Optional<PetriNet> createdNetOpt = importHelper.createNet("/query_lang_test.xml");
        assertTrue(createdNetOpt.isPresent());
        testCase = importHelper.createCase("test", createdNetOpt.get());
    }

    @Test
    public void queryResourceTypeTest() {
        assertEquals(QueryType.CASE, caseSearchService.getQueryResourceType());
    }

    @Test
    public void searchOneTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchOne((String) null));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchOne("cases: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchOne("process: identifier eq 'query_lang_test'"));

        Case result = caseSearchService.searchOne("case: title eq 'test'");
        assertNotNull(result);
        assertNotNull(result.getPetriNet());
        assertEquals(testCase.getStringId(), result.getStringId());

        setData(Map.of(QUERY_FIELD_ID, Map.of("value", "xxx", "type", FieldType.TEXT.getName())));
        Thread.sleep(2000);
        result = caseSearchService.searchOne("case: data." + QUERY_FIELD_ID + ".value eq 'xxx'");
        assertNotNull(result);
        assertNotNull(result.getPetriNet());
        assertEquals(testCase.getStringId(), result.getStringId());

        result = caseSearchService.searchOne("case: title eq 'wrong'");
        assertNull(result);
    }

    @Test
    public void searchAllTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchAll((String) null));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchAll("case: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.searchAll("processes: identifier eq 'query_lang_test'"));

        Page<Case> result = caseSearchService.searchAll("cases: title eq 'test'");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertNotNull(result.getContent().get(0).getPetriNet());
        assertEquals(testCase.getStringId(), result.getContent().get(0).getStringId());

        setData(Map.of(QUERY_FIELD_ID, Map.of("value", "xxx", "type", FieldType.TEXT.getName())));
        Thread.sleep(2000);
        result = caseSearchService.searchAll("cases: data." + QUERY_FIELD_ID + ".value eq 'xxx'");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertNotNull(result.getContent().get(0).getPetriNet());
        assertEquals(testCase.getStringId(), result.getContent().get(0).getStringId());

        result = caseSearchService.searchAll("cases: title eq 'wrong'");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void countTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.count((String) null));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.count("process: identifier eq 'query_lang_test'"));

        long result = caseSearchService.count("case: title eq 'test'");
        assertEquals(1, result);

        result = caseSearchService.count("cases: title eq 'test'");
        assertEquals(1, result);

        setData(Map.of(QUERY_FIELD_ID, Map.of("value", "xxx", "type", FieldType.TEXT.getName())));
        Thread.sleep(2000);
        result = caseSearchService.count("cases: data." + QUERY_FIELD_ID + ".value eq 'xxx'");
        assertEquals(1, result);

        result = caseSearchService.count("cases: title eq 'wrong'");
        assertEquals(0, result);
    }

    @Test
    public void existsTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.exists((String) null));
        assertThrows(IllegalArgumentException.class, () -> caseSearchService.exists("process: identifier eq 'query_lang_test'"));

        boolean result = caseSearchService.exists("case: title eq 'test'");
        assertTrue(result);

        result = caseSearchService.exists("cases: title eq 'test'");
        assertTrue(result);

        setData(Map.of(QUERY_FIELD_ID, Map.of("value", "xxx", "type", FieldType.TEXT.getName())));
        Thread.sleep(2000);
        result = caseSearchService.exists("cases: data." + QUERY_FIELD_ID + ".value eq 'xxx'");
        assertTrue(result);

        result = caseSearchService.exists("cases: title eq 'wrong'");
        assertFalse(result);
    }

    private void setData(Map<String, Map<String, String>> dataSet) {
        String taskId = testCase.getTasks().stream()
                .filter(taskPair -> taskPair.getTransition().equals("t1"))
                .map(TaskPair::getTask)
                .findFirst().get();
        testCase = importHelper.setTaskData(taskId, dataSet).getCase();
    }

}
