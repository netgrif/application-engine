package com.netgrif.application.engine.search;

import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.search.utils.MongoDbUtils;
import com.netgrif.application.engine.workflow.domain.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.netgrif.application.engine.search.utils.SearchUtils.evaluateQuery;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class QueryLangTest {
    public static final ObjectId GENERIC_OBJECT_ID = ObjectId.get();

    @Autowired
    MongoOperations mongoOperations;

    @Test
    public void testSimpleMongodbProcessQuery() {
        MongoDbUtils<PetriNet> mongoDbUtils = new MongoDbUtils<>(mongoOperations, PetriNet.class);

        // id comparison
        Predicate actual = evaluateQuery(String.format("process: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("process: id in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

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
        actual = evaluateQuery("process: version in (1.1.1, 2.2.2, 3.3.3)").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.in(List.of(v1, v2, v3));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version not in (1.1.1, 2.2.2, 3.3.3)").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.in(List.of(v1, v2, v3)).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version in (1.1.1 : 2.2.2)").getFullMongoQuery();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1)))));
        builder.and(QPetriNet.petriNet.version.major.lt(2)
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.lt(2)))
                .or(QPetriNet.petriNet.version.major.eq(2L).and(QPetriNet.petriNet.version.minor.eq(2L).and(QPetriNet.petriNet.version.patch.lt(2)))));
        expected = builder;

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version in [1.1.1 : 2.2.2]").getFullMongoQuery();
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
        Predicate expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).or(QPetriNet.petriNet.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or  title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).or(QPetriNet.petriNet.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").or(QPetriNet.petriNet.title.defaultValue.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID).and(QPetriNet.petriNet.title.defaultValue.eq("test").or(QPetriNet.petriNet.title.defaultValue.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or (title eq 'test1' and identifier eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QPetriNet.petriNet.id.eq(GENERIC_OBJECT_ID)
                .and(QPetriNet.petriNet.title.defaultValue.eq("test")
                        .or(QPetriNet.petriNet.title.defaultValue.eq("test1").and(QPetriNet.petriNet.identifier.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbCaseQuery() {
        MongoDbUtils<Case> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Case.class);

        // id comparison
        Predicate actual = evaluateQuery(String.format("case: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QCase.case$.id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

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

        actual = evaluateQuery("case: author contains 'test'").getFullMongoQuery();
        expected = QCase.case$.author.id.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: author in ('test', 'test1')").getFullMongoQuery();
        expected = QCase.case$.author.id.in("test", "test1");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // only available for elastic query
        // places comparison
        actual = evaluateQuery("case: places.p1.marking eq 1").getFullMongoQuery();
        assert actual == null;

        // task state comparison
        actual = evaluateQuery("case: tasks.t1.state eq enabled").getFullMongoQuery();
        assert actual == null;

        // task userId comparison
        actual = evaluateQuery("case: tasks.t1.userId eq 'test'").getFullMongoQuery();
        assert actual == null;

        // data value comparison
        actual = evaluateQuery("case: data.field1.value eq 'test'").getFullMongoQuery();
        assert actual == null;

        actual = evaluateQuery("case: data.field1.value contains 'test'").getFullMongoQuery();
        assert actual == null;

        actual = evaluateQuery("case: data.field1.value eq 1").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value lt 1").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value lte 1").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value gt 1").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value gte 1").getFullMongoQuery();
        assert actual == null;

        actual = evaluateQuery("case: data.field1.value eq 2011-12-03T10:15:30").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value lt 2011-12-03T10:15:30").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value lte 2011-12-03T10:15:30").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value gt 2011-12-03T10:15:30").getFullMongoQuery();
        assert actual == null;
        
        actual = evaluateQuery("case: data.field1.value gte 2011-12-03T10:15:30").getFullMongoQuery();
        assert actual == null;

        actual = evaluateQuery("case: data.field1.value eq true").getFullMongoQuery();
        assert actual == null;

        // data options comparison
        actual = evaluateQuery("case: data.field1.options eq 'test'").getFullMongoQuery();
        assert actual == null;

        actual = evaluateQuery("case: data.field1.options contains 'test'").getFullMongoQuery();
        assert actual == null;
    }

    @Test
    public void testComplexMongodbCaseQuery() {
        MongoDbUtils<Case> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Case.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("case: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).or(QCase.case$.title.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).or(QCase.case$.title.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID).and(QCase.case$.title.eq("test").or(QCase.case$.title.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or (title eq 'test1' and processIdentifier eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QCase.case$.id.eq(GENERIC_OBJECT_ID)
                .and(QCase.case$.title.eq("test")
                        .or(QCase.case$.title.eq("test1").and(QCase.case$.processIdentifier.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbTaskQuery() {
        MongoDbUtils<Task> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Task.class);

        // id comparison
        Predicate actual = evaluateQuery(String.format("task: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QTask.task.id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("task: id in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        // transitionId comparison
        checkStringComparison(mongoDbUtils, "task", "transitionId", QTask.task.transitionId);

        // title comparison
        checkStringComparison(mongoDbUtils, "task", "title", QTask.task.title.defaultValue);

        // state comparison
        actual = evaluateQuery("task: state eq enabled").getFullMongoQuery();
        expected = QTask.task.state.eq(State.ENABLED);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: state eq disabled").getFullMongoQuery();
        expected = QTask.task.state.eq(State.DISABLED);

        compareMongoQueries(mongoDbUtils, actual, expected);

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
        checkDateComparison(mongoDbUtils, "task", "lastAssign", QTask.task.lastAssigned);

        // lastFinish comparison
        checkDateComparison(mongoDbUtils, "task", "lastFinish", QTask.task.lastFinished);
    }

    @Test
    public void testComplexMongodbTaskQuery() {
        MongoDbUtils<Task> mongoDbUtils = new MongoDbUtils<>(mongoOperations, Task.class);

        // not comparison
        Predicate actual = evaluateQuery(String.format("task: id not eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QTask.task.id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).or(QTask.task.title.defaultValue.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).or(QTask.task.title.defaultValue.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").or(QTask.task.title.defaultValue.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID).and(QTask.task.title.defaultValue.eq("test").or(QTask.task.title.defaultValue.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or (title eq 'test1' and processId eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QTask.task.id.eq(GENERIC_OBJECT_ID)
                .and(QTask.task.title.defaultValue.eq("test")
                        .or(QTask.task.title.defaultValue.eq("test1").and(QTask.task.processId.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleMongodbUserQuery() {
        MongoDbUtils<User> mongoDbUtils = new MongoDbUtils<>(mongoOperations, User.class);

        // id comparison
        Predicate actual = evaluateQuery(String.format("user: id eq '%s'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        Predicate expected = QUser.user.id.eq(GENERIC_OBJECT_ID);

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery(String.format("user: id in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.in(GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);

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
        Predicate expected = QUser.user.id.eq(GENERIC_OBJECT_ID).not();

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // and not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).or(QUser.user.email.eq("test"));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // or not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email not eq 'test'", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).or(QUser.user.email.eq("test").not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").or(QUser.user.email.eq("test1")));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // parenthesis not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and not (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID).and(QUser.user.email.eq("test").or(QUser.user.email.eq("test1")).not());

        compareMongoQueries(mongoDbUtils, actual, expected);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or (email eq 'test1' and name eq 'test'))", GENERIC_OBJECT_ID)).getFullMongoQuery();
        expected = QUser.user.id.eq(GENERIC_OBJECT_ID)
                .and(QUser.user.email.eq("test")
                        .or(QUser.user.email.eq("test1").and(QUser.user.name.eq("test"))));

        compareMongoQueries(mongoDbUtils, actual, expected);
    }

    @Test
    public void testSimpleElasticProcessQuery() {
        // elastic query should be always null
        // id comparison
        String actual = evaluateQuery(String.format("process: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // identifier comparison
        actual = evaluateQuery("process: identifier eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("process: identifier contains 'test'").getFullElasticQuery();
        assert actual == null;

        // version comparison
        actual = evaluateQuery("process: version eq 1.1.1").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("process: version lt 1.1.1").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("process: version lte 1.1.1").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: version gt 1.1.1").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: version gte 1.1.1").getFullElasticQuery();
        assert actual == null;


        // title comparison
        actual = evaluateQuery("process: title eq 'test'").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: title contains 'test'").getFullElasticQuery();
        assert actual == null;


        // creationDate comparison
        actual = evaluateQuery("process: creationDate eq 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: creationDate lt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: creationDate lte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: creationDate gt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;


        actual = evaluateQuery("process: creationDate gte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testComplexElasticProcessQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("process: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis not comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;


        // nested parenthesis comparison
        actual = evaluateQuery(String.format("process: id eq '%s' and (title eq 'test' or (title eq 'test1' and identifier eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testSimpleElasticCaseQuery() {
        // id comparison
        String actual = evaluateQuery(String.format("case: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        String expected = String.format("stringId:%s", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        actual = evaluateQuery(String.format("case: id in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // processId comparison
        actual = evaluateQuery(String.format("case: processId eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("processId:%s", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        actual = evaluateQuery(String.format("case: processId in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("processId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // processIdentifier comparison
        checkStringComparisonElastic("case", "processIdentifier", "processIdentifier");

        // title comparison
        checkStringComparisonElastic("case", "title", "title");

        // creationDate comparison
        checkDateComparisonElastic("case", "creationDate", "creationDateSortable");

        // author comparison
        actual = evaluateQuery("case: author eq 'test'").getFullElasticQuery();
        expected = "author:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: author contains 'test'").getFullElasticQuery();
        expected = "author:*test*";
        assert expected.equals(actual);

        actual = evaluateQuery(String.format("case: author in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("author:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // places comparison
        checkNumberComparisonElastic("case", "places.p1.marking", "places.p1.marking");

        // task state comparison
        actual = evaluateQuery("case: tasks.t1.state eq enabled").getFullElasticQuery();
        expected = String.format("tasks.t1.state:%s", State.ENABLED);
        assert expected.equals(actual);

        actual = evaluateQuery("case: tasks.t1.state eq disabled").getFullElasticQuery();
        expected = String.format("tasks.t1.state:%s", State.DISABLED);
        assert expected.equals(actual);

        // task userId comparison
        actual = evaluateQuery("case: tasks.t1.userId eq 'test'").getFullElasticQuery();
        expected = "tasks.t1.userId:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: tasks.t1.userId contains 'test'").getFullElasticQuery();
        expected = "tasks.t1.userId:*test*";
        assert expected.equals(actual);

        actual = evaluateQuery(String.format("case: tasks.t1.userId in ('%s', '%s')", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("tasks.t1.userId:(%s OR %s)", GENERIC_OBJECT_ID, GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // data value comparison
        checkStringComparisonElastic("case", "data.field1.value", "dataSet.field1.textValue");

        checkNumberComparisonElastic("case", "data.field2.value", "dataSet.field2.numberValue");

        checkDateComparisonElastic("case", "data.field3.value", "dataSet.field3.timestampValue");

        actual = evaluateQuery("case: data.field1.value eq true").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:true";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value eq false").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:false";
        assert expected.equals(actual);

        // data options comparison
        checkStringComparisonElastic("case", "data.field1.options", "dataSet.field1.options");
    }

    @Test
    public void testComplexElasticCaseQuery() {
        // not comparison
        String actual = evaluateQuery(String.format("case: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        String expected = String.format("NOT stringId:%s", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // and comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND title:test", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // and not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND NOT title:test", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // or comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s OR title:test", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // or not comparison
        actual = evaluateQuery(String.format("case: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s OR NOT title:test", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND (title:test OR title:test1)", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("case: id eq '%s' and (title eq 'test' or (title eq 'test1' and processIdentifier eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        expected = String.format("stringId:%s AND (title:test OR (title:test1 AND processIdentifier:test))", GENERIC_OBJECT_ID);
        assert expected.equals(actual);
    }

    @Test
    public void testSimpleElasticTaskQuery() {
        // elastic query should be always null
        // id comparison
        String actual = evaluateQuery(String.format("task: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // transitionId comparison
        actual = evaluateQuery("task: transitionId eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: transitionId contains 'test'").getFullElasticQuery();
        assert actual == null;

        // title comparison
        actual = evaluateQuery("task: title eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: title contains 'test'").getFullElasticQuery();
        assert actual == null;

        // state comparison
        actual = evaluateQuery("task: state eq enabled").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: state eq disabled").getFullElasticQuery();
        assert actual == null;

        // userId comparison
        actual = evaluateQuery("task: userId eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: userId contains 'test'").getFullElasticQuery();
        assert actual == null;

        // caseId comparison
        actual = evaluateQuery("task: caseId eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: caseId contains 'test'").getFullElasticQuery();
        assert actual == null;

        // processId comparison
        actual = evaluateQuery("task: processId eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: processId contains 'test'").getFullElasticQuery();
        assert actual == null;

        // lastAssign comparison
        actual = evaluateQuery("task: lastAssign eq 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastAssign lt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastAssign lte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastAssign gt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastAssign gte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        // lastFinish comparison
        actual = evaluateQuery("task: lastFinish eq 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastFinish lt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastFinish lte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastFinish gt 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("task: lastFinish gte 2011-12-03T10:15:30").getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testComplexElasticTaskQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("task: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' or title not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis not comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and not (title eq 'test' or title eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("task: id eq '%s' and (title eq 'test' or (title eq 'test1' and processId eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testSimpleElasticUserQuery() {
        // elastic query should be always null
        // id comparison
        String actual = evaluateQuery(String.format("user: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // name comparison
        actual = evaluateQuery("user: name eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("user: name contains 'test'").getFullElasticQuery();
        assert actual == null;

        // surname comparison
        actual = evaluateQuery("user: surname eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("user: surname contains 'test'").getFullElasticQuery();
        assert actual == null;

        // email comparison
        actual = evaluateQuery("user: email eq 'test'").getFullElasticQuery();
        assert actual == null;

        actual = evaluateQuery("user: email contains 'test'").getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testComplexElasticUserQuery() {
        // elastic query should be always null
        // not comparison
        String actual = evaluateQuery(String.format("user: id not eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // and not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and email not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // or not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' or email not eq 'test'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // parenthesis not comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and not (email eq 'test' or email eq 'test1')", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;

        // nested parenthesis comparison
        actual = evaluateQuery(String.format("user: id eq '%s' and (email eq 'test' or (email eq 'test1' and name eq 'test'))", GENERIC_OBJECT_ID)).getFullElasticQuery();
        assert actual == null;
    }

    @Test
    public void testPagingQuery() {
        // default
        Pageable pageable = evaluateQuery("cases: processIdentifier eq 'test'").getPageable();
        assert pageable.getPageNumber() == 0;
        assert pageable.getPageSize() == 20;

        // page number only
        pageable = evaluateQuery("cases: processIdentifier eq 'test' page 2").getPageable();
        assert pageable.getPageNumber() == 2;
        assert pageable.getPageSize() == 20;

        // page number and page size
        pageable = evaluateQuery("cases: processIdentifier eq 'test' page 2 size 4").getPageable();
        assert pageable.getPageNumber() == 2;
        assert pageable.getPageSize() == 4;
    }

    @Test
    public void testProcessSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("processes: identifier eq 'test'").getPageable();
        assert !actual.getSort().isSorted();

        // default ordering asc
        actual = evaluateQuery("processes: identifier eq 'test' sort by id").getPageable();
        assert actual.getSort().isSorted();
        List<Sort.Order> orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        // set ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("processes: identifier eq 'test' sort by identifier desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("identifier");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("processes: identifier eq 'test' sort by title asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("title.defaultValue");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("processes: identifier eq 'test' sort by version asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("version");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("processes: identifier eq 'test' sort by creationDate asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("creationDate");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        // complex set ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id asc, title desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.defaultValue");
        assert orders.get(1).getDirection().equals(Sort.Direction.DESC);

        // complex default ordering
        actual = evaluateQuery("processes: identifier eq 'test' sort by id asc, title").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.defaultValue");
        assert orders.get(1).getDirection().equals(Sort.Direction.ASC);
    }

    @Test
    public void testCaseSortingMongoDbQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("cases: processIdentifier eq 'test'").getPageable();
        assert !actual.getSort().isSorted();

        // default ordering asc
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id").getPageable();
        assert actual.getSort().isSorted();
        List<Sort.Order> orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        // set ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by processIdentifier desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("processIdentifier");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by title asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("title");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by processId asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("petriNetObjectId");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by creationDate asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("creationDate");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by author desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("author.id");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        // complex set ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id asc, title desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title");
        assert orders.get(1).getDirection().equals(Sort.Direction.DESC);

        // complex default ordering
        actual = evaluateQuery("cases: processIdentifier eq 'test' sort by id asc, title").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title");
        assert orders.get(1).getDirection().equals(Sort.Direction.ASC);
    }

    @Test
    public void testCaseSortingElasticQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("cases: data.field1.value eq 'test'").getPageable();
        assert !actual.getSort().isSorted();

        // default ordering asc
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id").getPageable();
        assert actual.getSort().isSorted();
        List<Sort.Order> orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("stringId.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        // set ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("stringId.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by processIdentifier desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("processIdentifier.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by title asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("title.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by processId asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("processId.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by creationDate asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("creationDateSortable");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by author desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("author.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by places.p1.marking desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("places.p1.marking");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by tasks.t1.state desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("tasks.t1.state.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by tasks.t1.userId desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("tasks.t1.userId.keyword");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        // complex set ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id asc, title desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("stringId.keyword");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.keyword");
        assert orders.get(1).getDirection().equals(Sort.Direction.DESC);

        // complex default ordering
        actual = evaluateQuery("cases: data.field1.value eq 'test' sort by id asc, title").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("stringId.keyword");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.keyword");
        assert orders.get(1).getDirection().equals(Sort.Direction.ASC);
    }

    @Test
    public void testTaskSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("tasks: title eq 'test'").getPageable();
        assert !actual.getSort().isSorted();

        // default ordering asc
        actual = evaluateQuery("tasks: title eq 'test' sort by id").getPageable();
        assert actual.getSort().isSorted();
        List<Sort.Order> orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        // set ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("tasks: title eq 'test' sort by transitionId desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("transitionId");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("tasks: title eq 'test' sort by title asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("title.defaultValue");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("tasks: title eq 'test' sort by processId asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("processId");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("tasks: title eq 'test' sort by caseId asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("caseId");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("tasks: title eq 'test' sort by userId asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("userId");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("tasks: title eq 'test' sort by lastAssign asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("lastAssigned");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        actual = evaluateQuery("tasks: title eq 'test' sort by lastFinish desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("lastFinished");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        // complex set ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id asc, title desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.defaultValue");
        assert orders.get(1).getDirection().equals(Sort.Direction.DESC);

        // complex default ordering
        actual = evaluateQuery("tasks: title eq 'test' sort by id asc, title").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("title.defaultValue");
        assert orders.get(1).getDirection().equals(Sort.Direction.ASC);
    }

    @Test
    public void testUserSortingQuery() {
        // default (no sort)
        Pageable actual = evaluateQuery("users: name eq 'test'").getPageable();
        assert !actual.getSort().isSorted();

        // default ordering asc
        actual = evaluateQuery("users: name eq 'test' sort by id").getPageable();
        assert actual.getSort().isSorted();
        List<Sort.Order> orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;


        // set ordering
        actual = evaluateQuery("users: name eq 'test' sort by id desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("users: name eq 'test' sort by name desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("name");
        assert orders.get(0).getDirection() == Sort.Direction.DESC;

        actual = evaluateQuery("users: name eq 'test' sort by surname asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("surname");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        actual = evaluateQuery("users: name eq 'test' sort by email asc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 1;
        assert orders.get(0).getProperty().equals("email");
        assert orders.get(0).getDirection() == Sort.Direction.ASC;

        // complex set ordering
        actual = evaluateQuery("users: name eq 'test' sort by id asc, name desc").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("name");
        assert orders.get(1).getDirection().equals(Sort.Direction.DESC);

        // complex default ordering
        actual = evaluateQuery("users: name eq 'test' sort by id asc, name").getPageable();
        assert actual.getSort().isSorted();
        orders = actual.getSort().toList();
        assert orders.size() == 2;
        assert orders.get(0).getProperty().equals("id");
        assert orders.get(0).getDirection().equals(Sort.Direction.ASC);
        assert orders.get(1).getProperty().equals("name");
        assert orders.get(1).getDirection().equals(Sort.Direction.ASC);
    }

    @Test
    public void testProcessQueriesFail() {
        // using case, task, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: author eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: transitionId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastFinish eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: email eq 'test'"));
    }

    @Test
    public void testCaseQueriesFail() {
        // using process, task, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: transitionId eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastFinish eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: email eq 'test'"));
    }

    @Test
    public void testTaskQueriesFail() {
        // using process, case, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: creationDate eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: email eq 'test'"));
    }

    @Test
    public void testUserQueriesFail() {
        // using process, case, task attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: creationDate eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: author eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: transitionId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastFinish eq 2020-03-03"));
    }

    @Test
    public void testComparisonTypeFail() {
        // id comparison
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id lt 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id lte 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id gt 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: id gte 'test'"));

        // number comparison
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: places.p1.marking contains 1"));

        // date/datetime comparison
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: creationDate contains 2020-03-03"));

        // boolean comparison
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value contains true"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value lt true"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value lte true"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value gt true"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: data.field1.value gte true"));
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

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s contains 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:*test*", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lt 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<test", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lte 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=test", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gt 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>test", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gte 'test'", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=test", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in ('test1', 'test2', 'test3')", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(test1 OR test2 OR test3)", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in ('test1', 'test2', 'test3')", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(test1 OR test2 OR test3)", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in ('test1' : 'test2')", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>test1 AND %s:<test2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in ['test1' : 'test2']", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=test1 AND %s:<=test2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in ('test1' : 'test2']", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>test1 AND %s:<=test2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);
    }

    private static void checkNumberComparisonElastic(String resource, String attribute, String resultAttribute) {
        String actual = evaluateQuery(String.format("%s: %s eq 1", resource, attribute)).getFullElasticQuery();
        String expected = String.format("%s:1", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lt 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<1", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lte 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=1", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gt 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>1", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gte 1", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=1", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (1, 2, 3)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(1 OR 2 OR 3)", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (1, 2, 3)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(1 OR 2 OR 3)", resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (1 : 2)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>1 AND %s:<2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in [1 : 2]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=1 AND %s:<=2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (1 : 2]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>1 AND %s:<=2)", resultAttribute, resultAttribute);

        assert actual.equals(expected);
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

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lt 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s lte 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:<=%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gt 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s gte 2011-12-03T10:15:30", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:>=%s", resultAttribute, Timestamp.valueOf(date1).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date1).getTime(), Timestamp.valueOf(date2).getTime(), Timestamp.valueOf(date3).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30, 2011-12-03T11:15:30, 2011-12-03T12:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date1).getTime(), Timestamp.valueOf(date2).getTime(), Timestamp.valueOf(date3).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03T10:15:30 : 2011-12-03T11:15:30)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>%s AND %s:<%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03T10:15:30 : 2011-12-03T11:15:30]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date1).getTime(), resultAttribute, Timestamp.valueOf(date2).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("%s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date4).getTime(), Timestamp.valueOf(date5).getTime(), Timestamp.valueOf(date6).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03, 2011-12-03, 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT %s:(%s OR %s OR %s)", resultAttribute, Timestamp.valueOf(date4).getTime(), Timestamp.valueOf(date5).getTime(), Timestamp.valueOf(date6).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in (2011-12-03 : 2011-12-03)", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>%s AND %s:<%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s in [2011-12-03 : 2011-12-03]", resource, attribute)).getFullElasticQuery();
        expected = String.format("(%s:>=%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assert actual.equals(expected);

        actual = evaluateQuery(String.format("%s: %s not in (2011-12-03 : 2011-12-03]", resource, attribute)).getFullElasticQuery();
        expected = String.format("NOT (%s:>%s AND %s:<=%s)", resultAttribute, Timestamp.valueOf(date4).getTime(), resultAttribute, Timestamp.valueOf(date5).getTime());

        assert actual.equals(expected);
    }

    private static void compareMongoQueries(MongoDbUtils<?> mongoDbUtils, Predicate actual, Predicate expected) {
        Document actualDocument = mongoDbUtils.convertPredicateToDocument(actual);
        Document expectedDocument = mongoDbUtils.convertPredicateToDocument(expected);

        assert actualDocument.equals(expectedDocument);
    }
}
