package com.netgrif.application.engine.pfql;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.pfql.service.ISearchService;
import com.netgrif.application.engine.pfql.utils.MongoDbUtils;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.Task;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class QueryLangTest {

    public static final ObjectId GENERIC_OBJECT_ID = ObjectId.get();

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private ImportHelper helper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IUserService userService;

    @Test
    @SuppressWarnings("unchecked")
    public void testSearchService() throws InterruptedException {
        testHelper.truncateDbs();

        Optional<PetriNet> optionalPetriNet = helper.createNet("/pfql.xml");
        assertNotNull(optionalPetriNet);

        Object processAsObject = searchService.search("process: identifier == 'query_test'");
        assertNotNull(processAsObject);
        assertEquals(PetriNet.class, processAsObject.getClass());

        PetriNet process = (PetriNet) processAsObject;
        for (int i = 0; i < 10; i++) {
            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(i));
            helper.createCase(String.format("Test %02d", i), process, params);
        }

        Thread.sleep(2000);

        Object cases = searchService.search("cases: processIdentifier eq 'query_test' page 1 size 5 sort by title desc");
        assertNotNull(cases);
        assertEquals(PageImpl.class, cases.getClass());
        assertEquals(10, ((Page<Case>) cases).getTotalElements());

        Object case3 = searchService.search("case: processIdentifier eq 'query_test' and data.number_0.value == 3");
        assertNotNull(case3);
        assertEquals(Case.class, case3.getClass());
        assertEquals("Test 03", ((Case) case3).getTitle());
        assertEquals(3, ((Case) case3).getFieldValue("number_0"));

        Object case4 = searchService.search("case: processIdentifier eq 'query_test' and data.text_0.value == '4'");
        assertNotNull(case4);
        assertEquals(Case.class, case4.getClass());
        assertEquals("4", ((Case) case4).getFieldValue("text_0"));

        Object case5 = searchService.search("case: processIdentifier eq 'query_test' and data.boolean_0.value == true");
        assertNotNull(case5);
        assertEquals(Case.class, case5.getClass());
        assertEquals(true, ((Case) case5).getFieldValue("boolean_0"));

        cases = searchService.search("cases: processIdentifier eq 'query_test' and data.boolean_0.value == true");
        assertEquals(5, ((Page<Case>) cases).getTotalElements());

        cases = searchService.search("cases: processIdentifier eq 'query_test'    and data.boolean_0.value == true and data.text_0.value != '4'");
        assertEquals(4, ((Page<Case>) cases).getTotalElements());

        cases = searchService.search("cases: processIdentifier eq 'query_test' and author eq loggedUser.id");
        assertEquals(10, ((Page<Case>) cases).getTotalElements());
    }

    @Test
    public void testSimpleMongodbProcessQuery() {
        MongoDbUtils<PetriNet> mongoDbUtils = new MongoDbUtils<>(mongoOperations, PetriNet.class);

        // without comparison
        Predicate actual = evaluateQuery("processes").getFullMongoQuery();
        Predicate expected = new BooleanBuilder();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // id comparison
        actual = evaluateQuery(String.format("process: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("process: id in('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        // identifier comparison
        checkStringComparison(mongoDbUtils, "process", "identifier", QPetriNet.petriNet.identifier);

        // version comparison
        actual = evaluateQuery("process: version eq 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.eq(new Version(1, 1, 1));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version lt 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.lt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.lt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.lt(1))));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version lte 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.loe(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.loe(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.loe(1))));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version gt 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1))));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version gte 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.goe(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.goe(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.goe(1))));

        compareMongoQueries(mongoDbUtils, actual, expected);

        Version v1 = new Version(1, 1, 1);
        Version v2 = new Version(2, 2, 2);
        Version v3 = new Version(3, 3, 3);
        actual = evaluateQuery("process: version in(1.1.1, 2.2.2, 3.3.3)").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.in(List.of(v1, v2, v3));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version not in     (1.1.1, 2.2.2, 3.3.3)").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.in(List.of(v1, v2, v3)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version in(1.1.1:2.2.2)").getFullMongoQuery();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1)))));
        builder.and(QPetriNet.petriNet.version.major.lt(2)
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.lt(2)))
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.eq(2L).and(QPetriNet.petriNet.version.patch.lt(2)))));
        expected = builder;

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version in[1.1.1 :2.2.2]").getFullMongoQuery();
        builder = new BooleanBuilder();
        builder.and(QPetriNet.petriNet.version.major.goe(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.goe(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.goe(1)))));
        builder.and(QPetriNet.petriNet.version.major.loe(2)
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.loe(2)))
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.eq(2L).and(QPetriNet.petriNet.version.patch.loe(2)))));
        expected = builder;

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version not in (1.1.1 : 2.2.2]").getFullMongoQuery();
        builder = new BooleanBuilder();
        builder.and(QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1)))));
        builder.and(QPetriNet.petriNet.version.major.loe(2)
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.loe(2)))
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.eq(2L).and(QPetriNet.petriNet.version.patch.loe(2)))));
        expected = builder.not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // title comparison
        checkStringComparison(mongoDbUtils, "process", "title", QPetriNet.petriNet.title.defaultValue);

        // creationDate comparison
        checkDateComparison(mongoDbUtils, "process", "creationDate", QPetriNet.petriNet.creationDate);
    }

    @Test
    public void testComplexMongodbProcessQuery() {
        MongoDbUtils<PetriNet> mongoDbUtils = new MongoDbUtils<>(mongoOperations, PetriNet.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("process: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("process: id neq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        IUser systemUser = userService.getSystem();
        actual = evaluateQuery("process: id neq loggedUser.id").getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(new ObjectId(systemUser.getStringId())).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("process: id eq '%s' and title != 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).or(QPetriNet.petriNet.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or  title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).or(QPetriNet.petriNet.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").or(QPetriNet.petriNet.title.defaultValue.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").or(QPetriNet.petriNet.title.defaultValue.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or (title eq 'test1' and identifier eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet._id.eq(GENERIC_OBJECT_ID)
                .and(QPetriNet.petriNet.title.defaultValue.eq("test")
                        .or(QPetriNet.petriNet.title.defaultValue.eq("test1").and(QPetriNet.petriNet.identifier.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbCaseQuery() {
        MongoDbUtils<Case> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Case.class);

        // without comparison
        Predicate actual = evaluateQuery("cases").getFullMongoQuery();
        Predicate expected = new BooleanBuilder();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // id comparison
        actual = evaluateQuery(String.format("case: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery("case: id neq loggedUser.id").getFullMongoQuery();
        expected = QCase.case$._id.eq(new ObjectId(systemUser.getId())).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s', loggedUser.id, loggedUser.username, loggedUser.fullName)",
                GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID, new ObjectId(systemUser.getId()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s', loggedUser.username, loggedUser.fullName)",
                GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        // processId comparison
        actual = evaluateQuery(String.format("case: processId eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.petriNetObjectId.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: processId in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.petriNetObjectId.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        // processIdentifier comparison
        checkStringComparison(mongoDbUtils, "case", "processIdentifier", QCase.case$.processIdentifier);

        // title comparison
        checkStringComparison(mongoDbUtils, "case", "title", QCase.case$.title);

        // creationDate comparison
        checkDateComparison(mongoDbUtils, "case", "creationDate", QCase.case$.creationDate);

        // author comparison
        actual = evaluateQuery("case: author eq 'test'").getFullMongoQuery();
        expected = QCase.case$.author.id.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: author eq loggedUser.id").getFullMongoQuery();
        expected = QCase.case$.author.id.eq(systemUser.getId());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: author contains 'test'").getFullMongoQuery();
        expected = QCase.case$.author.id.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: author in ('test', 'test1', loggedUser.id, loggedUser.username, loggedUser.fullName)").getFullMongoQuery();
        expected = QCase.case$.author.id.in("test", "test1", systemUser.getId(), systemUser.getUsername(), systemUser.getFullName());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("cases: title in ('test1' : loggedUser.username)").getFullMongoQuery();
        expected = QCase.case$.title.gt("test1").and(QCase.case$.title.lt(systemUser.getUsername()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("cases: title in (loggedUser.username : 'test1')").getFullMongoQuery();
        expected = QCase.case$.title.gt(systemUser.getUsername()).and(QCase.case$.title.lt("test1"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("cases: title in (loggedUser.username : loggedUser.fullName)").getFullMongoQuery();
        expected = QCase.case$.title.gt(systemUser.getUsername()).and(QCase.case$.title.lt(systemUser.getFullName()));

        compareMongoQueries(mongoDbUtils, actual, expected);


        // only available for elastic query
        // places comparison
        actual = evaluateQuery("case: places.p1.marking eq 1").getFullMongoQuery();
        assertNull(actual);

        // task state comparison
        actual = evaluateQuery("case: tasks.t1.state eq enabled").getFullMongoQuery();
        assertNull(actual);

        // task userId comparison
        actual = evaluateQuery("case: tasks.t1.userId eq 'test'").getFullMongoQuery();
        assertNull(actual);

        // data value comparison
        actual = evaluateQuery("case: data.field1.value eq 'test'").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.name.value eq 'test'").getFullMongoQuery(); // name is a reserved keyword
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value contains 'test'").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value eq 1").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value lt 1").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value lte 1").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value gt 1").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value gte 1").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value eq 2011-12-03T10:15:30").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value lt 2011-12-03T10:15:30").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value lte 2011-12-03T10:15:30").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value gt 2011-12-03T10:15:30").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value gte 2011-12-03T10:15:30").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.value eq true").getFullMongoQuery();
        assertNull(actual);

        // data options comparison
        actual = evaluateQuery("case: data.field1.options eq 'test'").getFullMongoQuery();
        assertNull(actual);

        actual = evaluateQuery("case: data.field1.options contains 'test'").getFullMongoQuery();
        assertNull(actual);
    }

    @Test
    public void testComplexMongodbCaseQuery() {
        MongoDbUtils<Case> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Case.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("case: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id neq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id eq '%s' and title != 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).or(QCase.case$.title.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).or(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id eq '%s' or title neq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID).or(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or title eq 'test1' or title eq loggedUser.username)",
                GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID)
                .and(QCase.case$.title.eq("test").or(QCase.case$.title.eq("test1")).or(QCase.case$.title.eq(systemUser.getUsername())));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or (title eq 'test1' and processIdentifier eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$._id.eq(GENERIC_OBJECT_ID)
                .and(QCase.case$.title.eq("test")
                        .or(QCase.case$.title.eq("test1").and(QCase.case$.processIdentifier.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbTaskQuery() {
        MongoDbUtils<Task> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Task.class);

        // without comparison
        Predicate actual = evaluateQuery("tasks").getFullMongoQuery();
        Predicate expected = new BooleanBuilder();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // id comparison
        actual = evaluateQuery(String.format("task: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery(String.format("task: id in ('%s', '%s', loggedUser.id)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID, new ObjectId(systemUser.getId()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // transitionId comparison
        checkStringComparison(mongoDbUtils, "task", "transitionId", QTask.task.transitionId);

        // title comparison
        checkStringComparison(mongoDbUtils, "task", "title", QTask.task.title.defaultValue);

        // state comparison
//        TODO: fix
//        actual = evaluateQuery("task: state eq enabled").getFullMongoQuery();
//        expected = QTask.task.state.eq(State.ENABLED);

//        compareMongoQueries(mongoDbUtils, actual, expected);

//        actual = evaluateQuery("task: state eq disabled").getFullMongoQuery();
//        expected = QTask.task.state.eq(State.DISABLED);

//        compareMongoQueries(mongoDbUtils, actual, expected);

        // userId comparison
        actual = evaluateQuery("task: userId eq 'test'").getFullMongoQuery();
        expected = QTask.task.userId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: userId contains 'test'").getFullMongoQuery();
        expected = QTask.task.userId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: userId in ('test', 'test1')").getFullMongoQuery();
        expected = QTask.task.userId.in("test", "test1");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // caseId comparison
        actual = evaluateQuery("task: caseId eq 'test'").getFullMongoQuery();
        expected = QTask.task.caseId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: caseId contains 'test'").getFullMongoQuery();
        expected = QTask.task.caseId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: caseId in ('test', 'test1')").getFullMongoQuery();
        expected = QTask.task.caseId.in("test", "test1");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // processId comparison
        actual = evaluateQuery("task: processId eq 'test'").getFullMongoQuery();
        expected = QTask.task.processId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: processId contains 'test'").getFullMongoQuery();
        expected = QTask.task.processId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: processId in ('test', 'test1')").getFullMongoQuery();
        expected = QTask.task.processId.in("test", "test1");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // lastAssign comparison
//        TODO: fix
//        checkDateComparison(mongoDbUtils, "task", "lastAssign", QTask.task.lastAssigned);

        // lastFinish comparison
//        checkDateComparison(mongoDbUtils, "task", "lastFinish", QTask.task.lastFinished);
    }

    @Test
    public void testComplexMongodbTaskQuery() {
        MongoDbUtils<Task> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Task.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("task: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QTask.task._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("task: id != '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("task: id eq '%s' and title neq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).or(QTask.task.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).or(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("task: id eq '%s' or title neq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).or(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").or(QTask.task.title.defaultValue.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").or(QTask.task.title.defaultValue.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or (title eq 'test1' and processId eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task._id.eq(GENERIC_OBJECT_ID)
                .and(QTask.task.title.defaultValue.eq("test")
                        .or(QTask.task.title.defaultValue.eq("test1").and(QTask.task.processId.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbUserQuery() {
        MongoDbUtils<User> mongoDbUtils = new MongoDbUtils<>(mongoOperations, User.class);

        // without comparison
        Predicate actual = evaluateQuery("users").getFullMongoQuery();
        Predicate expected = new BooleanBuilder();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // id comparison
        actual = evaluateQuery(String.format("user: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery("user: id eq loggedUser.id").getFullMongoQuery();
        expected = QUser.user._id.eq(new ObjectId(systemUser.getId()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("user: id in ('%s', '%s', loggEduser.id)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID, new ObjectId(systemUser.getId()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // name comparison
        checkStringComparison(mongoDbUtils, "user", "name", QUser.user.name);

        // surname comparison
        checkStringComparison(mongoDbUtils, "user", "surname", QUser.user.surname);

        // email comparison
        checkStringComparison(mongoDbUtils, "user", "email", QUser.user.email);
    }

    @Test
    public void testComplexMongodbUserQuery() {
        MongoDbUtils<User> mongoDbUtils = new MongoDbUtils<>(mongoOperations, User.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("user: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QUser.user._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("user: id != '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery(String.format("user: id eq '%s' and email eq loggedUser.username", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq(systemUser.getUsername()));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("user: id eq '%s' and email neq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).or(QUser.user.email.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).or(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("user: id eq '%s' or email != 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).or(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").or(QUser.user.email.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and not (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").or(QUser.user.email.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or (email eq 'test1' and name eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user._id.eq(GENERIC_OBJECT_ID)
                .and(QUser.user.email.eq("test")
                        .or(QUser.user.email.eq("test1").and(QUser.user.name.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleElasticProcessQuery() {
        // without comparison
        String actual = evaluateQuery("processes").getFullElasticQuery();
        assertEquals("*", actual);

        // elastic query should be always null
        // id comparison
        actual = evaluateQuery(String.format("process: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // identifier comparison
        actual = evaluateQuery("process: identifier eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("process: identifier contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // version comparison
        actual = evaluateQuery("process: version eq 1.1.1").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("process: version lt 1.1.1").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("process: version lte 1.1.1").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: version gt 1.1.1").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: version gte 1.1.1").getFullElasticQuery();
        assertNull(actual);


        // title comparison
        actual = evaluateQuery("process: title eq 'test'").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: title contains 'test'").getFullElasticQuery();
        assertNull(actual);


        // creationDate comparison
        actual = evaluateQuery("process: creationDate eq 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: creationDate lt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: creationDate lte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: creationDate gt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);


        actual = evaluateQuery("process: creationDate gte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testComplexElasticProcessQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("process: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);
        assertNull(actual);

        actual = evaluateQuery(String.format("process: id neq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("process: id eq '%s' and title != 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("process: id eq '%s' or title != 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);


        // nested parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or (title eq 'test1' and identifier eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testSimpleElasticCaseQuery() {
        // without comparison
        String actual = evaluateQuery("cases").getFullElasticQuery();
        String expected = "*";
        assertEquals(expected, actual);

        // id comparison
        actual = evaluateQuery(String.format("case: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        LoggedUser systemUser = userService.getSystem().transformToLoggedUser();
        actual = evaluateQuery("case: id neq loggedUser.id").getFullElasticQuery();
        expected = String.format("NOT stringId:%s", systemUser.getId());
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s', loggedUser.id, loggedUser.username, loggedUser.fullname)",
                GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:(%s OR %s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID, systemUser.getId());
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s', loggedUser.username, loggedUser.fullname)",
                GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // processId comparison
        actual = evaluateQuery(String.format("case: processId eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("processId:%s", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: processId in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("processId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // processIdentifier comparison
        checkStringComparisonElastic("case", "processIdentifier", "processIdentifier");

        // title comparison
        checkStringComparisonElastic("case", "title", "title");

        // creationDate comparison
        checkDateComparisonElastic("case", "creationDate", "creationDateSortable");

        // author comparison
        actual = evaluateQuery("case: author eq 'test'").getFullElasticQuery();
        expected = "author:test";
        assertEquals(expected, actual);

        actual = evaluateQuery("case: author eq loggedUser.id").getFullElasticQuery();
        expected = String.format("author:%s", systemUser.getId());
        assertEquals(expected, actual);

        actual = evaluateQuery("case: author contains 'test'").getFullElasticQuery();
        expected = "author:*test*";
        assertEquals(expected, actual);

        actual = evaluateQuery("case: author contains loggedUser.id").getFullElasticQuery();
        expected = String.format("author:*%s*", systemUser.getId());
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: author in ('%s', '%s', loggedUser.id)", GENERIC_OBJECT_ID,
                GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("author:(%s OR %s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID, new ObjectId(systemUser.getId()));
        assertEquals(expected, actual);

        // places comparison
        checkNumberComparisonElastic("case", "places.p1.marking", "places.p1.marking");

        // task state comparison
//        TODO: fix
//        actual = evaluateQuery("case: tasks.t1.state eq enabled").getFullElasticQuery();
//        expected = String.format("tasks.t1.state:%s", State.ENABLED);
//        assertEquals(expected, actual);
//        actual = evaluateQuery("case: tasks.t1.state eq disabled").getFullElasticQuery();
//        expected = String.format("tasks.t1.state:%s", State.DISABLED);
//        assertEquals(expected, actual);

        // task userId comparison
        actual = evaluateQuery("case: tasks.t1.userId eq 'test'").getFullElasticQuery();
        expected = "tasks.t1.userId:test";
        assertEquals(expected, actual);

        actual = evaluateQuery("case: tasks.t1.userId contains 'test'").getFullElasticQuery();
        expected = "tasks.t1.userId:*test*";
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: tasks.t1.userId in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("tasks.t1.userId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // data value comparison
        checkStringComparisonElastic("case", "data.field1.value", "dataSet.field1.textValue");

        checkNumberComparisonElastic("case", "data.field2.value", "dataSet.field2.numberValue");

        checkDateComparisonElastic("case", "data.field3.value", "dataSet.field3.timestampValue");

        actual = evaluateQuery("case: data.field1.value eq true").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:true";
        assertEquals(expected, actual);

        actual = evaluateQuery("case: data.field1.value eq loggedUser.anonymous").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:false";
        assertEquals(expected, actual);

        actual = evaluateQuery("case: data.field1.value eq false").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:false";
        assertEquals(expected, actual);

        // data options comparison
        checkStringComparisonElastic("case", "data.field1.options", "dataSet.field1.options");

        actual = evaluateQuery("cases: title in ('test1' : loggedUser.username)").getFullElasticQuery();
        expected = String.format("(title:>test1 AND title:<%s)", systemUser.getUsername());

        assertEquals(expected, actual);

        actual = evaluateQuery("cases: title in (loggedUser.username : 'test1')").getFullElasticQuery();
        expected = String.format("(title:>%s AND title:<test1)", systemUser.getUsername());

        assertEquals(expected, actual);

        actual = evaluateQuery("cases: title in (loggedUser.username : loggedUser.fullName)").getFullElasticQuery();
        expected = String.format("(title:>%s AND title:<%s)", systemUser.getUsername(), systemUser.getFullName());

        assertEquals(expected, actual);
    }

    @Test
    public void testComplexElasticCaseQuery() {
        // not comparison
        String actual = evaluateQuery(String.format("case: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        String expected = String.format("NOT stringId:%s", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: id neq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("NOT stringId:%s", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // and comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // and not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND NOT title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: id eq '%s' and title != 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND NOT title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // or comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s OR title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // or not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s OR NOT title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("case: id eq '%s' or title neq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s OR NOT title:test", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND (title:test OR title:test1)", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or (title eq 'test1' and processIdentifier eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND (title:test OR (title:test1 AND processIdentifier:test))", GENERIC_OBJECT_ID);
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleElasticTaskQuery() {
        // without comparison
        String actual = evaluateQuery("tasks").getFullElasticQuery();
        assertEquals("*", actual);

        // elastic query should be always null
        // id comparison
        actual = evaluateQuery(String.format("task: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // transitionId comparison
        actual = evaluateQuery("task: transitionId eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: transitionId contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // title comparison
        actual = evaluateQuery("task: title eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: title contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // state comparison
        actual = evaluateQuery("task: state eq enabled").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: state eq disabled").getFullElasticQuery();
        assertNull(actual);

        // userId comparison
        actual = evaluateQuery("task: userId eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: userId contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // caseId comparison
        actual = evaluateQuery("task: caseId eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: caseId contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // processId comparison
        actual = evaluateQuery("task: processId eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: processId contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // lastAssign comparison
        actual = evaluateQuery("task: lastAssign eq 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastAssign lt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastAssign lte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastAssign gt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastAssign gte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        // lastFinish comparison
        actual = evaluateQuery("task: lastFinish eq 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastFinish lt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastFinish lte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastFinish gt 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("task: lastFinish gte 2011-12-03T10:15:30").getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testComplexElasticTaskQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("task: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("task: id neq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("task: id eq '%s' and title != 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("task: id eq '%s' or title neq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or (title eq 'test1' and processId eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testSimpleElasticUserQuery() {
        // without comparison
        String actual = evaluateQuery("users").getFullElasticQuery();
        assertEquals("*", actual);

        // elastic query should be always null
        // id comparison
        actual = evaluateQuery(String.format("user: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // name comparison
        actual = evaluateQuery("user: name eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("user: name contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // surname comparison
        actual = evaluateQuery("user: surname eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("user: surname contains 'test'").getFullElasticQuery();
        assertNull(actual);

        // email comparison
        actual = evaluateQuery("user: email eq 'test'").getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery("user: email contains 'test'").getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testComplexElasticUserQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("user: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("user: id != '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // and not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("user: id eq '%s' and email neq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // or not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        actual = evaluateQuery(String.format("user: id eq '%s' or email != 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and not (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or (email eq 'test1' and name eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assertNull(actual);
    }

    @Test
    public void testPagingQuery() {
        Pageable pageable = evaluateQuery("cases: processIdentifier eq 'test'").getPageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        pageable = evaluateQuery("cases: processIdentifier eq 'test' page 2").getPageable();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        pageable = evaluateQuery("cases: processIdentifier eq 'test' page 2 size 4").getPageable();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(4, pageable.getPageSize());

        pageable = evaluateQuery("cases: page 2 size 4").getPageable();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(4, pageable.getPageSize());
    }

    @Test
    public void testProcessSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("processes: identifier eq 'test'").getPageable();
        assertFalse(actual.getSort().isSorted());

        // default ordering asc
        actual = evaluateQuery("processes: identifier eq 'test' sort by id").getPageable();
        assertTrue(actual.getSort().isSorted());
        List<Sort.Order> orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        // set ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("processes: identifier eq 'test' sort by identifier desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("identifier", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("processes: identifier eq 'test' sort by title asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("title.defaultValue", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("processes: identifier eq 'test' sort by version asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("version", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("processes: identifier eq 'test' sort by creationDate asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("creationDate", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        // complex set ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id asc, title desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("processes: sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
    }

    @Test
    public void testCaseSortingMongoDbQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("cases: processIdentifier eq 'test'").getPageable();
        assertFalse(actual.getSort().isSorted());

        // default ordering asc
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id").getPageable();
        assertTrue(actual.getSort().isSorted());
        List<Sort.Order> orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        // set ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by processIdentifier desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("processIdentifier", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by title asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("title", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by processId asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("petriNetObjectId", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by creationDate asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("creationDate", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by author desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("author.id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        // complex set ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id asc, title desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("cases: sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
    }

    @Test
    public void testCaseSortingElasticQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("cases: data.field1.value eq 'test'").getPageable();
        assertFalse(actual.getSort().isSorted());

        // default ordering asc
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id").getPageable();
        assertTrue(actual.getSort().isSorted());
        List<Sort.Order> orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("stringId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        // set ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("stringId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by processIdentifier desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("processIdentifier.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by title asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("title.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by processId asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("processId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by creationDate asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("creationDateSortable", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by author desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("author.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by places.p1.marking desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("places.p1.marking", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by tasks.t1.state desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("tasks.t1.state.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by tasks.t1.userId desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("tasks.t1.userId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        // complex set ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id asc, title desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("stringId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.keyword", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("stringId.keyword", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.keyword", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
    }

    @Test
    public void testTaskSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("tasks: title eq 'test'").getPageable();
        assertFalse(actual.getSort().isSorted());

        // default ordering asc
        actual = evaluateQuery("tasks: title eq 'test' sort by id").getPageable();
        assertTrue(actual.getSort().isSorted());
        List<Sort.Order> orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        // set ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by transitionId desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("transitionId", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by title asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("title.defaultValue", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by processId asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("processId", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by caseId asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("caseId", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by userId asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("userId", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("tasks: title eq 'test' sort by lastAssign asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("lastAssigned", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        actual = evaluateQuery("tasks: title eq 'test' sort by lastFinish desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("lastFinished", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        // complex set ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id asc, title desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("tasks: sort by id asc, title").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("title.defaultValue", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
    }

    @Test
    public void testUserSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("users: name eq 'test'").getPageable();
        assertFalse(actual.getSort().isSorted());

        // default ordering asc
        actual = evaluateQuery("users: name eq 'test' sort by id").getPageable();
        assertTrue(actual.getSort().isSorted());
        List<Sort.Order> orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());


        // set ordering
        actual = evaluateQuery("users: name eq 'test' sort by id desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("users: name eq 'test' sort by name desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("name", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        actual = evaluateQuery("users: name eq 'test' sort by surname asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("surname", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        actual = evaluateQuery("users: name eq 'test' sort by email asc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("email", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

        // complex set ordering
        actual = evaluateQuery("users: name eq 'test' sort by id asc, name desc").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("name", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("users: name eq 'test' sort by id asc, name").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("name", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());

        // complex default ordering
        actual = evaluateQuery("users: sort by id asc, name").getPageable();
        assertTrue(actual.getSort().isSorted());
        orders = actual.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("name", orders.get(1).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(1).getDirection());
    }

    @Test
    public void testProcessQueriesFail() {
        // using case, task, user attributes
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processIdentifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: author eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: places.p1.marking eq 1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.value eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.options contains 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: transitionId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: caseId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastAssign eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastFinish eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: name eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: surname eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process page 2"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process:"));
    }

    @Test
    public void testCaseQueriesFail() {
        // using process, task, user attributes
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: version eq 1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: transitionId eq 1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: caseId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastAssign eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastFinish eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: name eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: surname eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case page 2"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case:"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: creationDate eq loggedUser.id"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: processIdentifier eq loggedUser.anonymous"));
    }

    @Test
    public void testTaskQueriesFail() {
        // using process, case, user attributes
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: identifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: version eq 1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: creationDate eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: processIdentifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: places.p1.marking eq 1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.value eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.options contains 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: name eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: surname eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task email eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task page 2"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task:"));
    }

    @Test
    public void testUserQueriesFail() {
        // using process, case, task attributes
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: identifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: version eq 1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: creationDate eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processIdentifier eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: author eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: places.p1.marking eq 1"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.value eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.options contains 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: transitionId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: state eq enabled"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: userId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: caseId eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastAssign eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastFinish eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user lastFinish eq 2020-03-03"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user page 2"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user:"));
    }

    @Test
    public void testComparisonTypeFail() {
        // id comparison
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id contains 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id lt 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id lte 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id gt 'test'"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id gte 'test'"));

        // number comparison
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: places.p1.marking contains 1"));

        // date/datetime comparison
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: creationDate contains 2020-03-03"));

        // boolean comparison
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value contains true"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value lt true"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value lte true"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value gt true"));
        assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value gte true"));
    }

    private static void checkStringComparison(MongoDbUtils<?> mongoDbUtils, String resource, String attribute, StringPath stringPath) {
        Predicate actual = evaluateQuery(String.format("%s: %s eq 'test'", resource, attribute)).getFullMongoQuery();
        Predicate expected = stringPath.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s contains 'test'", resource, attribute)).getFullMongoQuery();
        expected = stringPath.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s lt 'test'", resource, attribute)).getFullMongoQuery();
        expected = stringPath.lt("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s lte 'test'", resource, attribute)).getFullMongoQuery();
        expected = stringPath.loe("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s gt 'test'", resource, attribute)).getFullMongoQuery();
        expected = stringPath.gt("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s gte 'test'", resource, attribute)).getFullMongoQuery();
        expected = stringPath.goe("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in ('test1', 'test2', 'test3')", resource, attribute)).getFullMongoQuery();
        expected = stringPath.in(List.of("test1", "test2", "test3"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in ('test1', 'test2', 'test3')", resource, attribute)).getFullMongoQuery();
        expected = stringPath.in(List.of("test1", "test2", "test3")).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in ('test1' : 'test2')", resource, attribute)).getFullMongoQuery();
        expected = stringPath.gt("test1").and(stringPath.lt("test2"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in ['test1' : 'test2']", resource, attribute)).getFullMongoQuery();
        expected = stringPath.goe("test1").and(stringPath.loe("test2"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in ('test1' : 'test2']", resource, attribute)).getFullMongoQuery();
        expected = stringPath.gt("test1").and(stringPath.loe("test2")).not();

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    private static void checkDateComparison(MongoDbUtils<?> mongoDbUtils, String resource, String attribute, DateTimePath<LocalDateTime> dateTimePath) {
        LocalDateTime date1 = LocalDateTime.of(2011, 12, 3, 10, 15, 30);
        LocalDateTime date2 = LocalDateTime.of(2011, 12, 3, 11, 15, 30);
        LocalDateTime date3 = LocalDateTime.of(2011, 12, 3, 12, 15, 30);
        LocalDateTime date4 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);
        LocalDateTime date5 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);
        LocalDateTime date6 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);

        Predicate actual = evaluateQuery(String.format("%s: %s eq 2011-12-03T10:15:30", resource, attribute)).getFullMongoQuery();
        Predicate expected = dateTimePath.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s lt 2011-12-03T10:15:30", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s lte 2011-12-03T10:15:30", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.loe(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s gt 2011-12-03T10:15:30", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s gte 2011-12-03T10:15:30", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.goe(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.in(List.of(date1, date2, date3));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.in(List.of(date1, date2, date3)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30 : 2011-12-03T11:15:30)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.gt(date1).and(dateTimePath.lt(date2));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.goe(date1).and(dateTimePath.loe(date2));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.gt(date1).and(dateTimePath.loe(date2)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.in(List.of(date4, date5, date6));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.in(List.of(date4, date5, date6)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03 : 2011-12-03)", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.gt(date4).and(dateTimePath.lt(date5));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03 : 2011-12-03]", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.goe(date4).and(dateTimePath.loe(date5));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03 : 2011-12-03]", resource, attribute)).getFullMongoQuery();
        expected = dateTimePath.gt(date4).and(dateTimePath.loe(date5)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    private static void checkStringComparisonElastic(String resource, String attribute, String resultAttribute) {
        String actual = evaluateQuery(String.format("%s: %s eq 'test'", resource, attribute)).getFullElasticQuery();
        String expected = String.format("%s:test", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s contains 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:*test*", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lt 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<test", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lte 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=test", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gt 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>test", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gte 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=test", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in ('test1', 'test2', 'test3')", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(test1 OR test2 OR test3)", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in ('test1', 'test2', 'test3')", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(test1 OR test2 OR test3)", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in ('test1' : 'test2')", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>test1 AND %s:<test2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in ['test1' : 'test2']", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=test1 AND %s:<=test2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in ('test1' : 'test2']", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>test1 AND %s:<=test2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);
    }

    private static void checkNumberComparisonElastic(String resource, String attribute, String resultAttribute) {
        String actual = evaluateQuery(String.format("%s: %s eq 1", resource, attribute)).getFullElasticQuery();
        String expected = String.format("%s:1", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lt 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<1", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lte 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=1", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gt 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>1", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gte 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=1", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (1, 2, 3)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(1 OR 2 OR 3)", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (1, 2, 3)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(1 OR 2 OR 3)", resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (1 : 2)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>1 AND %s:<2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in [1 : 2]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=1 AND %s:<=2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (1 : 2]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>1 AND %s:<=2)", resultAttribute, resultAttribute);

        assertEquals(expected, actual);
    }

    private static void checkDateComparisonElastic(String resource, String attribute, String resultAttribute) {
        LocalDateTime date1 = LocalDateTime.of(2011, 12, 3, 10, 15, 30);
        LocalDateTime date2 = LocalDateTime.of(2011, 12, 3, 11, 15, 30);
        LocalDateTime date3 = LocalDateTime.of(2011, 12, 3, 12, 15, 30);
        LocalDateTime date4 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);
        LocalDateTime date5 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);
        LocalDateTime date6 = LocalDateTime.of(2011, 12, 3, 12, 0, 0);

        String actual = evaluateQuery(String.format("%s: %s eq 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        String expected = String.format("%s:%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lt 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s lte 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gt 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s gte 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date1).getTime(), Timestamp.valueOf(date2).getTime(), Timestamp.valueOf(date3).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date1).getTime(), Timestamp.valueOf(date2).getTime(), Timestamp.valueOf(date3).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30 : 2011-12-03T11:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>%s AND %s:<%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date4).getTime(), Timestamp.valueOf(date5).getTime(), Timestamp.valueOf(date6).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date4).getTime(), Timestamp.valueOf(date5).getTime(), Timestamp.valueOf(date6).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03 : 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>%s AND %s:<%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03 : 2011-12-03]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assertEquals(expected, actual);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03 : 2011-12-03]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assertEquals(expected, actual);
    }

    private static void compareMongoQueries(MongoDbUtils<?> mongoDbUtils, Predicate actual, Predicate expected) {
        Document actualDocument = mongoDbUtils.convertPredicateToDocument(actual);
        Document expectedDocument = mongoDbUtils.convertPredicateToDocument(expected);

        assertEquals(expectedDocument, actualDocument);
    }
}
