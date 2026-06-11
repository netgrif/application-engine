package com.netgrif.application.engine.pfql;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.processresource.ProcessSearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ProcessSearchServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ProcessSearchService processSearchService;

    @Autowired
    private ImportHelper importHelper;

    private PetriNet testNet;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        Optional<PetriNet> createdNetOpt = importHelper.createNet("/query_lang_test.xml");
        assertTrue(createdNetOpt.isPresent());
        testNet = createdNetOpt.get();
    }

    @Test
    public void queryResourceTypeTest() {
        assertEquals(QueryType.PROCESS, processSearchService.getQueryResourceType());
    }

    @Test
    public void searchOneTest() {
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchOne((String) null));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchOne("processes: identifier eq 'xxx'"));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchOne("case: processIdentifier eq 'xxx'"));

        PetriNet result = processSearchService.searchOne("process: identifier eq 'query_lang_test'");
        assertNotNull(result);
        assertEquals(testNet.getStringId(), result.getStringId());

        result = processSearchService.searchOne("process: identifier eq 'wrong'");
        assertNull(result);
    }

    @Test
    public void searchAllTest() {
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchAll((String) null));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchAll("process: identifier eq 'xxx'"));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.searchAll("cases: processIdentifier eq 'xxx'"));

        Page<PetriNet> result = processSearchService.searchAll("processes: identifier eq 'query_lang_test'");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(20, result.getPageable().getPageSize());
        assertEquals(testNet.getStringId(), result.getContent().get(0).getStringId());

        result = processSearchService.searchAll("processes: identifier eq 'query_lang_test' page 0 size 67");
        assertNotNull(result);
        assertEquals(67, result.getPageable().getPageSize());

        result = processSearchService.searchAll("processes: identifier eq 'wrong'");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void countTest() {
        assertThrows(IllegalArgumentException.class, () -> processSearchService.count((String) null));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.count("case: identifier eq 'xxx'"));

        long result = processSearchService.count("process: identifier eq 'query_lang_test'");
        assertEquals(1, result);

        result = processSearchService.count("processes: identifier eq 'query_lang_test'");
        assertEquals(1, result);

        result = processSearchService.count("processes: identifier eq 'query_lang_test'");
        assertEquals(1, result);

        result = processSearchService.count("processes: identifier eq 'wrong'");
        assertEquals(0, result);
    }

    @Test
    public void existsTest() {
        assertThrows(IllegalArgumentException.class, () -> processSearchService.exists((String) null));
        assertThrows(IllegalArgumentException.class, () -> processSearchService.exists("case: identifier eq 'xxx'"));

        boolean result = processSearchService.exists("process: identifier eq 'query_lang_test'");
        assertTrue(result);

        result = processSearchService.exists("processes: identifier eq 'query_lang_test'");
        assertTrue(result);

        result = processSearchService.exists("processes: identifier eq 'query_lang_test'");
        assertTrue(result);

        result = processSearchService.exists("processes: identifier eq 'wrong'");
        assertFalse(result);
    }
}
