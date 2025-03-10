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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.netgrif.application.engine.search.utils.SearchTestUtils.*;
import static com.netgrif.application.engine.search.utils.SearchUtils.toDateString;
import static com.netgrif.application.engine.search.utils.SearchUtils.toDateTimeString;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchCaseTest {

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

    private IUser createUser(String name, String surname, String email) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get("user")};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return importHelper.createUser(user, authorities, processRoles);
    }

    private void searchAndCompare(String query, Case expectedResult) {
        long count = searchService.count(query);
        assert count == 1;

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, Case.class), expectedResult, Case::getStringId);
    }

    private void searchAndCompare(String query, List<Case> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, Case.class), expected, Case::getStringId);
    }

    private void searchAndCompareAsList(String query, List<Case> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObjectList(actual, Case.class), expected, Case::getStringId);
    }

    private void searchAndCompareAsListInOrder(String query, List<Case> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObjectList(actual, Case.class), expected, Case::getStringId);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test2", net);

        String query = String.format("case: id eq '%s'", case1.getStringId());

        searchAndCompare(query, case1);

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by id", net.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by id desc", net.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2));
        searchAndCompareAsListInOrder(querySort2, List.of(case2, case1));
    }

    @Test
    public void testSearchByProcessId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net2);

        String queryEq = String.format("case: processId eq '%s'", net.getStringId());
        String queryOther = String.format("case: processId eq '%s'", net2.getStringId());
        String queryMore = String.format("cases: processId eq '%s'", net.getStringId());

        searchAndCompare(queryEq, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier in ('%s', '%s') sort by processId", net.getIdentifier(), net2.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier in ('%s', '%s') sort by processId desc", net.getIdentifier(), net2.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2, case3));
        searchAndCompareAsListInOrder(querySort2, List.of(case3, case1, case2));
    }

    @Test
    public void testSearchByProcessIdentifier() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");
        PetriNet net3 = importPetriNet("search/search_test3.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net2);
        Case case4 = importHelper.createCase("Search Test3", net3);

        String query = String.format("case: processIdentifier eq '%s'", net.getIdentifier());
        String queryOther = String.format("case: processIdentifier eq '%s'", net2.getIdentifier());
        String queryMore = String.format("cases: processIdentifier eq '%s'", net.getIdentifier());

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // in list
        String queryInList = String.format("cases: processIdentifier in ('%s', '%s', '%s')", net.getIdentifier(), net2.getIdentifier(), net3.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(case1, case2, case3, case4));

        // in range
        String queryInRange = String.format("cases: processIdentifier in <'%s' : '%s')", net.getIdentifier(), net3.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(case1, case2, case3));

        // sort
        String querySort = String.format("cases: processIdentifier in ('%s', '%s') sort by processIdentifier", net.getIdentifier(), net2.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier in ('%s', '%s') sort by processIdentifier desc", net.getIdentifier(), net2.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2, case3));
        searchAndCompareAsListInOrder(querySort2, List.of(case3, case1, case2));
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

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // in list
        String queryInList = String.format("cases: processIdentifier eq '%s' and title in ('%s', '%s')", net.getIdentifier(), case1.getTitle(), case3.getTitle());
        searchAndCompareAsList(queryInList, List.of(case1, case2, case3));

        // in range
        String queryInRange = String.format("cases: processIdentifier eq '%s' and title in <'%s' : '%s')", net.getIdentifier(), case1.getTitle(), case3.getTitle());
        searchAndCompareAsList(queryInRange, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by title", net.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by title desc", net.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2, case3));
        searchAndCompareAsListInOrder(querySort2, List.of(case3, case1, case2));
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

        searchAndCompare(queryEq, case1);
        searchAndCompareAsList(queryLt, List.of(case1, case2));
        searchAndCompareAsList(queryLte, List.of(case1, case2, case3));
        searchAndCompareAsList(queryGt, List.of(case2, case3));
        searchAndCompareAsList(queryGte, List.of(case1, case2, case3));

        // in list
        String queryInList = String.format("cases: processIdentifier eq '%s' and creationDate in (%s, %s)", net.getIdentifier(), toDateTimeString(case1.getCreationDate()), toDateTimeString(case3.getCreationDate()));
        searchAndCompareAsList(queryInList, List.of(case1, case3));

        // in range
        String queryInRange = String.format("cases: processIdentifier eq '%s' and creationDate in <%s : %s)", net.getIdentifier(), toDateTimeString(case1.getCreationDate()), toDateTimeString(case3.getCreationDate()));
        searchAndCompareAsList(queryInRange, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by creationDate", net.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by creationDate desc", net.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2, case3));
        searchAndCompareAsListInOrder(querySort2, List.of(case3, case2, case1));
    }

    @Test
    public void testSearchByAuthor() {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
        IUser user2 = createUser("Name2", "Surname2", "Email2");
        Case case1 = importHelper.createCase("Search Test", net, user1.transformToLoggedUser());
        Case case2 = importHelper.createCase("Search Test", net, user1.transformToLoggedUser());
        Case case3 = importHelper.createCase("Search Test2", net, user2.transformToLoggedUser());

        String query = String.format("case: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user1.getStringId());
        String queryOther = String.format("case: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user2.getStringId());
        String queryMore = String.format("cases: processIdentifier eq '%s' and author eq '%s'", net.getIdentifier(), user1.getStringId());

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by author", net.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by author desc", net.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case1, case2, case3));
        searchAndCompareAsListInOrder(querySort2, List.of(case3, case1, case2));
    }

    @Test
    public void testSearchByPlaces() throws InterruptedException {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
        Case case1 = importHelper.createCase("Search Test1", net);
        Case case2 = importHelper.createCase("Search Test2", net);
        Case case3 = importHelper.createCase("Search Test3", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String query = String.format("case: processIdentifier eq '%s' AND places.p2.marking eq %s", net.getIdentifier(), 1);
        String queryOther = String.format("case: processIdentifier eq '%s' AND places.p1.marking eq %s", net.getIdentifier(), 1);
        String queryMore = String.format("cases: processIdentifier eq '%s' AND places.p2.marking eq %s", net.getIdentifier(), 1);

        Thread.sleep(3000);

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // in list
        String queryInList = String.format("cases: processIdentifier eq '%s' and places.p1.marking in (1)", net.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(case3));

        // in range
        String queryInRange = String.format("cases: processIdentifier eq '%s' and places.p1.marking in <0 : 1>", net.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(case3));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by places.p2.marking", net.getIdentifier());
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by places.p2.marking desc", net.getIdentifier());

        searchAndCompareAsListInOrder(querySort, List.of(case3, case1, case2));
        searchAndCompareAsListInOrder(querySort2, List.of(case1, case2, case3));
    }

    @Test
    public void testSearchByTaskState() throws InterruptedException { // todo NAE-1997: not indexing tasks.state
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
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

        Thread.sleep(3000);

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by tasks.%s.state", net.getIdentifier(), TEST_TRANSITION_ID);
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by tasks.%s.state desc", net.getIdentifier(), TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(case3, case1, case2));
        searchAndCompareAsListInOrder(querySort2, List.of(case1, case2, case3));
    }

    @Test
    public void testSearchByTaskUserId() { // todo NAE-1997: not indexing tasks.userId
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
        IUser user2 = createUser("Name2", "Surname2", "Email2");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case3.getStringId(), user2.transformToLoggedUser());

        String query = String.format("case: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user1.getStringId());
        String queryOther = String.format("case: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user2.getStringId());
        String queryMore = String.format("cases: tasks.%s.userId eq '%s'", TEST_TRANSITION_ID, user1.getStringId());

        searchAndCompare(query, List.of(case1, case2));
        searchAndCompare(queryOther, case3);
        searchAndCompareAsList(queryMore, List.of(case1, case2));

        // sort
        String querySort = String.format("cases: processIdentifier eq '%s' sort by tasks.%s.userId", net.getIdentifier(), TEST_TRANSITION_ID);
        String querySort2 = String.format("cases: processIdentifier eq '%s' sort by tasks.%s.userId desc", net.getIdentifier(), TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(case3, case1, case2));
        searchAndCompareAsListInOrder(querySort2, List.of(case1, case2, case3));
    }

    @Test
    public void testSearchByDataValue() throws InterruptedException {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1");

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
        case2 = importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser()).getCase();

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

        // text
        searchAndCompare(queryTextEq, case1);
        searchAndCompare(queryTextContains, case1);

        // boolean
        searchAndCompare(queryBoolean, case1);

        // number
        searchAndCompare(queryNumberEq, case1);
        searchAndCompare(queryNumberLt, List.of(case1, case2));
        searchAndCompare(queryNumberLte, List.of(case1, case2));
        searchAndCompare(queryNumberGt, case1);
        searchAndCompare(queryNumberGte, case1);

        // date
        searchAndCompare(queryDateEq, case1);
        searchAndCompare(queryDateLt, List.of(case1, case2));
        searchAndCompare(queryDateLte, List.of(case1, case2));
        searchAndCompare(queryDateGt, case1);
        searchAndCompare(queryDateGte, case1);

        // datetime
        searchAndCompare(queryDateTimeEq, case1);
        searchAndCompare(queryDateTimeLt, List.of(case1, case2));
        searchAndCompare(queryDateTimeLte, List.of(case1, case2));
        searchAndCompare(queryDateTimeGt, case1);
        searchAndCompare(queryDateTimeGte, case1);

        // enumeration/multichoice
        // todo NAE-1997: should use keyValue, textValue represents only value
//        searchAndCompare(queryEnumerationEq, case1);
//        searchAndCompare(queryEnumerationContains, case1);
//
//        searchAndCompare(queryMultichoiceEq, case1);
//        searchAndCompare(queryMultichoiceContains, case1);

        // in list text
        String queryInList = String.format("cases: processIdentifier eq '%s' and data.text_immediate.value in ('test', 'other')", net.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(case1, case2));

        // in range text
        String queryInRange = String.format("cases: processIdentifier eq '%s' and data.text_immediate.value in <'other' : 'test')", net.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(case2));

        // in list number
        queryInList = String.format("cases: processIdentifier eq '%s' and data.number_immediate.value in (2, 54)", net.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(case1, case2));

        // in range number
        queryInRange = String.format("cases: processIdentifier eq '%s' and data.number_immediate.value in <2 : 54)", net.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(case2));

        // in list date
        queryInList = String.format("cases: processIdentifier eq '%s' and data.date_immediate.value in (%s, %s)", net.getIdentifier(), toDateString(LocalDateTime.now()), toDateString(LocalDateTime.now().minusDays(5)));
        searchAndCompareAsList(queryInList, List.of(case1, case2));

        // in range date
        queryInRange = String.format("cases: processIdentifier eq '%s' and data.date_immediate.value in <%s : %s)", net.getIdentifier(), toDateString(LocalDateTime.now().minusDays(10)), toDateString(LocalDateTime.now().plusDays(1)));
        searchAndCompareAsList(queryInRange, List.of(case1, case2));

        // in list datetime
        LocalDateTime localDateTime1 = (LocalDateTime) case1.getDataSet().get("date_time_immediate").getRawValue();
        LocalDateTime localDateTime2 = (LocalDateTime) case2.getDataSet().get("date_time_immediate").getRawValue();
        queryInList = String.format("cases: processIdentifier eq '%s' and data.date_time_immediate.value in (%s, %s)", net.getIdentifier(), toDateTimeString(localDateTime1), toDateTimeString(localDateTime2));
        searchAndCompareAsList(queryInList, List.of(case1, case2));

        // in range datetime
        queryInRange = String.format("cases: processIdentifier eq '%s' and data.date_time_immediate.value in <%s : %s)", net.getIdentifier(), toDateTimeString(localDateTime2), toDateTimeString(localDateTime1));
        searchAndCompareAsList(queryInRange, List.of(case2));

        // todo NAE-1997: sort by data - indexation change needed
    }

    @Test
    public void testSearchByDataOptions() throws InterruptedException {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case case1 = importHelper.createCase("Search Test", net);

        String queryEq = String.format("case: data.enumeration_map_immediate.options eq '%s'", "key1");
        String queryContains = String.format("case: data.enumeration_map_immediate.options contains '%s'", "key1");

        Thread.sleep(3000);

        searchAndCompare(queryEq, case1);
        searchAndCompare(queryContains, case1);

        // in list
        String queryInList = String.format("cases: processIdentifier eq '%s' and data.enumeration_map_immediate.options in ('key1')", net.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(case1));

        // in range
        String queryInRange = String.format("cases: processIdentifier eq '%s' and data.enumeration_map_immediate.options in <'key1' : 'key2')", net.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(case1));

        // todo NAE-1997: sort by data - indexation change needed
    }

    @Test
    public void testPagination() {
        PetriNet net = importPetriNet("search/search_test.xml");
        List<Case> cases = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            cases.add(importHelper.createCase("Search Test", net));
        }

        String queryOne = String.format("case: processIdentifier eq '%s'", "search_test");
        String queryMore = String.format("cases: processIdentifier eq '%s'", "search_test");
        String queryMoreCustomPagination = String.format("cases: processIdentifier eq '%s' page 1 size 5", "search_test");

        long count = searchService.count(queryOne);
        assert count == 50;

        Object actual = searchService.search(queryOne);
        compareById(convertToObject(actual, Case.class), cases.get(0), Case::getStringId);

        actual = searchService.search(queryMore);
        compareById(convertToObjectList(actual, Case.class), cases.subList(0, 19), Case::getStringId);

        actual = searchService.search(queryMoreCustomPagination);
        compareById(convertToObjectList(actual, Case.class), cases.subList(5, 9), Case::getStringId);
    }
}
