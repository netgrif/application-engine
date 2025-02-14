package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.search.utils.SearchUtils;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.utils.SearchUtils.toDateTimeString;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchCaseTest {
    @Autowired
    private IWorkflowService workflowService;

    public static final String TEST_TRANSITION_ID = "search_test_t1";

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ISearchService searchService;

    private Map<String, Authority> auths;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs();
        auths = importHelper.createAuthorities(Map.of("user", Authority.user, "admin", Authority.admin));
    }

    private PetriNet importPetriNet(String fileName) {
        PetriNet testNet = importHelper.createNet(fileName).orElse(null);
        assert testNet != null;
        return testNet;
    }

    private IUser createUser(String name, String surname, String email, String authority) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get(authority)};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return importHelper.createUser(user, authorities, processRoles);
    }

    private static Case convertToCase(Object caseObject) {
        assert caseObject instanceof Case;
        return (Case) caseObject;
    }

    private static List<Case> convertToCaseList(Object caseListObject) {
        assert caseListObject instanceof List<?>;
        for (Object caseObject : (List<?>) caseListObject) {
            assert caseObject instanceof Case;
        }

        return (List<Case>) caseListObject;
    }

    private void compareCases(Case actual, Case expected) {
        assert actual.getStringId().equals(expected.getStringId());
    }

    private void compareCases(Case actual, List<Case> expected) {
        List<String> expectedStringIds = expected.stream().map(Case::getStringId).collect(Collectors.toList());

        assert expectedStringIds.contains(actual.getStringId());
    }

    private void compareCases(List<Case> actual, List<Case> expected) {
        List<String> actualStringIds = actual.stream().map(Case::getStringId).collect(Collectors.toList());
        List<String> expectedStringIds = expected.stream().map(Case::getStringId).collect(Collectors.toList());

        assert actualStringIds.containsAll(expectedStringIds);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case caze = importHelper.createCase("Search Test", net);

        String query = String.format("case: id eq '%s'", caze.getStringId());

        long count = searchService.count(query);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), caze);
    }

    @Test
    public void testSearchByProcessId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net2);

        String query = String.format("case: processId eq '%s'", net.getStringId());
        String queryOther = String.format("case: processId eq '%s'", net2.getStringId());
        String queryMore = String.format("cases: processId eq '%s'", net.getStringId());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByProcessIdentifier() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net2);

        String query = String.format("case: processIdentifier eq '%s'", net.getIdentifier());
        String queryOther = String.format("case: processIdentifier eq '%s'", net2.getIdentifier());
        String queryMore = String.format("cases: processIdentifier eq '%s'", net.getIdentifier());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByTitle() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        String query = String.format("case: title eq '%s'", case1.getTitle());
        String queryOther = String.format("case: title eq '%s'", case3.getTitle());
        String queryMore = String.format("cases: title eq '%s'", case1.getTitle());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByCreationDate() {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test", net);

        String queryEq = String.format("case: creationDate eq %s", toDateTimeString(case1.getCreationDate()));
        String queryLt = String.format("cases: processIdentifier eq '%s' and creationDate lt %s", net.getIdentifier(), toDateTimeString(case3.getCreationDate()));
        String queryLte = String.format("cases: processIdentifier eq '%s' and creationDate lte %s", net.getIdentifier(), toDateTimeString(case3.getCreationDate()));
        String queryGt = String.format("cases: processIdentifier eq '%s' and creationDate gt %s", net.getIdentifier(), toDateTimeString(case1.getCreationDate()));
        String queryGte = String.format("cases: processIdentifier eq '%s' and creationDate gte %s", net.getIdentifier(), toDateTimeString(case1.getCreationDate()));

        long count = searchService.count(queryEq);
        assert count == 1;

        Object foundCase = searchService.search(queryEq);
        compareCases(convertToCase(foundCase), case1);

        count = searchService.count(queryLt);
        assert count == 2;

        Object cases = searchService.search(queryLt);
        compareCases(convertToCaseList(cases), List.of(case1, case2));

        count = searchService.count(queryLte);
        assert count == 3;

        cases = searchService.search(queryLte);
        compareCases(convertToCaseList(cases), List.of(case1, case2, case3));

        count = searchService.count(queryGt);
        assert count == 2;

        cases = searchService.search(queryGt);
        compareCases(convertToCaseList(cases), List.of(case2, case3));

        count = searchService.count(queryGte);
        assert count == 3;

        cases = searchService.search(queryGte);
        compareCases(convertToCaseList(cases), List.of(case1, case2, case3));
    }

    @Test
    public void testSearchByAuthor() {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");
        IUser user2 = createUser("Name2", "Surname2", "Email2", "user");

        Case case1 = importHelper.createCase("Search Test", net, user1.transformToLoggedUser());
        Case case2 = importHelper.createCase("Search Test", net, user1.transformToLoggedUser());
        Case case3 = importHelper.createCase("Search Test2", net, user2.transformToLoggedUser());

        String query = String.format("case: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user1.getStringId());
        String queryOther = String.format("case: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user2.getStringId());
        String queryMore = String.format("cases: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user1.getStringId());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByPlaces() throws InterruptedException {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String query = String.format("case: processIdentifier eq '%s' AND places.p2.marking eq %s", net.getIdentifier(), 1);
        String queryOther = String.format("case: processIdentifier eq '%s' AND places.p1.marking eq %s", net.getIdentifier(), 1);
        String queryMore = String.format("cases: processIdentifier eq '%s' AND places.p2.marking eq %s", net.getIdentifier(), 1);

        Thread.sleep(3000);

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByTaskState() { // todo NAE-1997: not indexing tasks.state
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String query = String.format("case: tasks.%s.state eq %s", TEST_TRANSITION_ID, "disabled");
        String queryOther = String.format("case: tasks.%s.state eq %s", TEST_TRANSITION_ID, "enabled");
        String queryMore = String.format("cases: tasks.%s.state eq %s", TEST_TRANSITION_ID, "disabled");

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByTaskUserId() { // todo NAE-1997: not indexing tasks.userId
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");
        IUser user2 = createUser("Name2", "Surname2", "Email2", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case3.getStringId(), user2.transformToLoggedUser());

        String query = String.format("case: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user1.getStringId());
        String queryOther = String.format("case: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user2.getStringId());
        String queryMore = String.format("cases: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user1.getStringId());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundCase = searchService.search(query);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryOther);
        compareCases(convertToCase(foundCase), case3);

        Object cases = searchService.search(queryMore);
        compareCases(convertToCaseList(cases), List.of(case1, case2));
    }

    @Test
    public void testSearchByDataValue() throws InterruptedException {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test2", net);

        BooleanField booleanFalse = new BooleanField();
        booleanFalse.setRawValue(false);
        TextField textField = new TextField();
        textField.setRawValue("other");
        Map<String, I18nString> options = Map.of("test1", new I18nString("Test1"), "test2", new I18nString("Test2"), "test3", new I18nString("Test3"));
        EnumerationMapField enumerationMapField = new EnumerationMapField();
        enumerationMapField.setOptions(options);
        enumerationMapField.setRawValue("test1");
        MultichoiceMapField multichoiceMapField = new MultichoiceMapField();
        multichoiceMapField.setOptions(options);
        multichoiceMapField.setRawValue(Set.of("test1", "test2"));
        NumberField numberField = new NumberField();
        numberField.setRawValue(2.0);
        DateField dateField = new DateField();
        dateField.setRawValue(LocalDate.now().minusDays(5));
        DateTimeField dateTimeField = new DateTimeField();
        dateTimeField.setRawValue(LocalDateTime.now().minusDays(5));

        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.setTaskData("Test", case2.getStringId(), new DataSet(Map.of(
                "boolean_immediate", booleanFalse,
                "text_immediate", textField,
                "number_immediate", numberField,
                "multichoice_map_immediate", multichoiceMapField,
                "enumeration_map_immediate", enumerationMapField,
                "date_immediate", dateField,
                "date_time_immediate", dateTimeField
        )));
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String queryTextEq = String.format("case: data.text_immediate.value eq %s", "'test'");
        String queryTextContains = String.format("case: data.text_immediate.value contains %s", "'es'");
        String queryBoolean = String.format("case: data.boolean_immediate.value eq %s", "true");
        String queryEnumerationEq = String.format("case: data.enumeration_map_immediate.value eq %s", "'key2'");
        String queryEnumerationContains = String.format("case: data.enumeration_map_immediate.value contains %s", "'ey2'");
        String queryMultichoiceEq = String.format("case: data.multichoice_map_immediate.value eq %s", "'key2'");
        String queryMultichoiceContains = String.format("case: data.multichoice_map_immediate.value contains %s", "'ey2'");
        String queryNumberEq = String.format("case: data.number_immediate.value eq %s", 54);
        String queryNumberLt = String.format("case: data.number_immediate.value lt %s", 55);
        String queryNumberLte = String.format("case: data.number_immediate.value lte %s", 55);
        String queryNumberGt = String.format("case: data.number_immediate.value gt %s", 53);
        String queryNumberGte = String.format("case: data.number_immediate.value gte %s", 53);
        String queryDateEq = String.format("case: data.date_immediate.value eq %s", SearchUtils.toDateString(LocalDate.now()));
        String queryDateLt = String.format("case: data.date_immediate.value lt %s", SearchUtils.toDateString(LocalDate.now().plusDays(1)));
        String queryDateLte = String.format("case: data.date_immediate.value lte %s", SearchUtils.toDateString(LocalDate.now().plusDays(1)));
        String queryDateGt = String.format("case: data.date_immediate.value gt %s", SearchUtils.toDateString(LocalDate.now().minusDays(1)));
        String queryDateGte = String.format("case: data.date_immediate.value gte %s", SearchUtils.toDateString(LocalDate.now().minusDays(1)));
        String queryDateTimeEq = String.format("case: data.date_time_immediate.value eq %s", SearchUtils.toDateTimeString((LocalDateTime) case1.getDataSet().get("date_time_immediate").getRawValue()));
        String queryDateTimeLt = String.format("case: data.date_time_immediate.value lt %s", SearchUtils.toDateTimeString(LocalDateTime.now().plusMinutes(1)));
        String queryDateTimeLte = String.format("case: data.date_time_immediate.value lte %s", SearchUtils.toDateTimeString(LocalDateTime.now().plusMinutes(1)));
        String queryDateTimeGt = String.format("case: data.date_time_immediate.value gt %s", SearchUtils.toDateTimeString(LocalDateTime.now().minusMinutes(1)));
        String queryDateTimeGte = String.format("case: data.date_time_immediate.value gte %s", SearchUtils.toDateTimeString(LocalDateTime.now().minusMinutes(1)));

        Thread.sleep(3000);

        long count = searchService.count(queryTextEq);
        assert count == 1;

        count = searchService.count(queryTextContains);
        assert count == 1;

        count = searchService.count(queryBoolean);
        assert count == 1;

        // todo NAE-1997: should use keyValue, textValue represents only value
//        count = searchService.count(queryEnumerationEq);
//        assert count == 1;
//
//        count = searchService.count(queryEnumerationContains);
//        assert count == 1;
//
//        count = searchService.count(queryMultichoiceEq);
//        assert count == 1;
//
//        count = searchService.count(queryMultichoiceContains);
//        assert count == 1;

        count = searchService.count(queryNumberEq);
        assert count == 1;

        count = searchService.count(queryNumberLt);
        assert count == 2;

        count = searchService.count(queryNumberLte);
        assert count == 2;

        count = searchService.count(queryNumberGt);
        assert count == 1;

        count = searchService.count(queryNumberGte);
        assert count == 1;

        count = searchService.count(queryDateEq);
        assert count == 1;

        count = searchService.count(queryDateLt);
        assert count == 2;

        count = searchService.count(queryDateLte);
        assert count == 2;

        count = searchService.count(queryDateGt);
        assert count == 1;

        count = searchService.count(queryDateGte);
        assert count == 1;

        count = searchService.count(queryDateTimeEq);
        assert count == 1;

        count = searchService.count(queryDateTimeLt);
        assert count == 2;

        count = searchService.count(queryDateTimeLte);
        assert count == 2;

        count = searchService.count(queryDateTimeGt);
        assert count == 1;

        count = searchService.count(queryDateTimeGte);
        assert count == 1;

        Object foundCase = searchService.search(queryTextEq);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryTextContains);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryBoolean);
        compareCases(convertToCase(foundCase), case1);

//        foundCase = searchService.search(queryEnumerationEq);
//        compareCases(convertToCase(foundCase), case1);
//
//        foundCase = searchService.search(queryEnumerationContains);
//        compareCases(convertToCase(foundCase), case1);
//
//        foundCase = searchService.search(queryMultichoiceEq);
//        compareCases(convertToCase(foundCase), case1);
//
//        foundCase = searchService.search(queryMultichoiceContains);
//        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryNumberEq);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryNumberLt);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryNumberLte);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryNumberGt);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryNumberGte);
        compareCases(convertToCase(foundCase), case1);

//        foundCase = searchService.search(queryDateEq);
//        compareCases(convertToCase(foundCase), case1);
//
//        foundCase = searchService.search(queryDateLt);
//        compareCases(convertToCase(foundCase), List.of(case1, case2));
//
//        foundCase = searchService.search(queryDateLte);
//        compareCases(convertToCase(foundCase), List.of(case1, case2));
//
//        foundCase = searchService.search(queryDateGt);
//        compareCases(convertToCase(foundCase), case1);
//
//        foundCase = searchService.search(queryDateGte);
//        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryDateTimeEq);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryDateTimeLt);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryDateTimeLte);
        compareCases(convertToCase(foundCase), List.of(case1, case2));

        foundCase = searchService.search(queryDateTimeGt);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryDateTimeGte);
        compareCases(convertToCase(foundCase), case1);
    }

    @Test
    public void testSearchByDataOptions() {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case case1 = importHelper.createCase("Search Test", net);

        String queryEq = String.format("case: data.enumeration_map_immediate.options eq '%s'", "key1");
        String queryContains = String.format("case: data.enumeration_map_immediate.options contains '%s'", "key1");

        long count = searchService.count(queryEq);
        assert count == 1;

        count = searchService.count(queryContains);
        assert count == 1;

        Object foundCase = searchService.search(queryEq);
        compareCases(convertToCase(foundCase), case1);

        foundCase = searchService.search(queryContains);
        compareCases(convertToCase(foundCase), case1);
    }
}
