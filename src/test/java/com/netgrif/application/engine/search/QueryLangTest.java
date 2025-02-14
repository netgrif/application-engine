package com.netgrif.application.engine.search;

import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.search.utils.MongoDbUtils;
import com.netgrif.application.engine.workflow.domain.*;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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

        // identifier comparison
        actual = evaluateQuery("process: identifier eq 'test'").getFullMongoQuery();
        expected = QPetriNet.petriNet.identifier.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: identifier contains 'test'").getFullMongoQuery();
        expected = QPetriNet.petriNet.identifier.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

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
        expected = QPetriNet.petriNet.version.major.lt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.lt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.lt(1))))
                .or(QPetriNet.petriNet.version.eq(new Version(1, 1, 1)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version gt 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1))));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: version gte 1.1.1").getFullMongoQuery();
        expected = QPetriNet.petriNet.version.major.gt(1)
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.gt(1)))
                .or(QPetriNet.petriNet.version.major.eq(1L).and(QPetriNet.petriNet.version.minor.eq(1L).and(QPetriNet.petriNet.version.patch.gt(1))))
                .or(QPetriNet.petriNet.version.eq(new Version(1, 1, 1)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // title comparison
        actual = evaluateQuery("process: title eq 'test'").getFullMongoQuery();
        expected = QPetriNet.petriNet.title.defaultValue.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: title contains 'test'").getFullMongoQuery();
        expected = QPetriNet.petriNet.title.defaultValue.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // creationDate comparison
        actual = evaluateQuery("process: creationDate eq 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QPetriNet.petriNet.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: creationDate lt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QPetriNet.petriNet.creationDate.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: creationDate lte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QPetriNet.petriNet.creationDate.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QPetriNet.petriNet.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: creationDate gt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QPetriNet.petriNet.creationDate.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("process: creationDate gte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QPetriNet.petriNet.creationDate.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QPetriNet.petriNet.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);
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

        // processId comparison
        actual = evaluateQuery("case: processId eq 'test'").getFullMongoQuery();
        expected = QCase.case$.petriNetId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: processId contains 'test'").getFullMongoQuery();
        expected = QCase.case$.petriNetId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // processIdentifier comparison
        actual = evaluateQuery("case: processIdentifier eq 'test'").getFullMongoQuery();
        expected = QCase.case$.processIdentifier.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: processIdentifier contains 'test'").getFullMongoQuery();
        expected = QCase.case$.processIdentifier.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // title comparison
        actual = evaluateQuery("case: title eq 'test'").getFullMongoQuery();
        expected = QCase.case$.title.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: title contains 'test'").getFullMongoQuery();
        expected = QCase.case$.title.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // creationDate comparison
        actual = evaluateQuery("case: creationDate eq 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QCase.case$.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: creationDate lt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QCase.case$.creationDate.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: creationDate lte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QCase.case$.creationDate.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QCase.case$.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: creationDate gt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QCase.case$.creationDate.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: creationDate gte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QCase.case$.creationDate.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QCase.case$.creationDate.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // author comparison
        actual = evaluateQuery("case: author eq 'test'").getFullMongoQuery();
        expected = QCase.case$.author.id.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("case: author contains 'test'").getFullMongoQuery();
        expected = QCase.case$.author.id.contains("test");

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

        // transitionId comparison
        actual = evaluateQuery("task: transitionId eq 'test'").getFullMongoQuery();
        expected = QTask.task.transitionId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: transitionId contains 'test'").getFullMongoQuery();
        expected = QTask.task.transitionId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // title comparison
        actual = evaluateQuery("task: title eq 'test'").getFullMongoQuery();
        expected = QTask.task.title.defaultValue.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: title contains 'test'").getFullMongoQuery();
        expected = QTask.task.title.defaultValue.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

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

        // caseId comparison
        actual = evaluateQuery("task: caseId eq 'test'").getFullMongoQuery();
        expected = QTask.task.caseId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: caseId contains 'test'").getFullMongoQuery();
        expected = QTask.task.caseId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // processId comparison
        actual = evaluateQuery("task: processId eq 'test'").getFullMongoQuery();
        expected = QTask.task.processId.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: processId contains 'test'").getFullMongoQuery();
        expected = QTask.task.processId.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // lastAssign comparison
        actual = evaluateQuery("task: lastAssign eq 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastAssigned.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastAssign lt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastAssigned.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastAssign lte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastAssigned.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QTask.task.lastAssigned.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastAssign gt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastAssigned.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastAssign gte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastAssigned.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QTask.task.lastAssigned.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        // lastFinish comparison
        actual = evaluateQuery("task: lastFinish eq 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastFinished.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastFinish lt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastFinished.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastFinish lte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastFinished.lt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QTask.task.lastFinished.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastFinish gt 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastFinished.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("task: lastFinish gte 2011-12-03T10:15:30").getFullMongoQuery();
        expected = QTask.task.lastFinished.gt(LocalDateTime.of(2011, 12, 3, 10, 15, 30))
                .or(QTask.task.lastFinished.eq(LocalDateTime.of(2011, 12, 3, 10, 15, 30)));

        compareMongoQueries(mongoDbUtils, actual, expected);
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

        // name comparison
        actual = evaluateQuery("user: name eq 'test'").getFullMongoQuery();
        expected = QUser.user.name.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("user: name contains 'test'").getFullMongoQuery();
        expected = QUser.user.name.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // surname comparison
        actual = evaluateQuery("user: surname eq 'test'").getFullMongoQuery();
        expected = QUser.user.surname.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("user: surname contains 'test'").getFullMongoQuery();
        expected = QUser.user.surname.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        // email comparison
        actual = evaluateQuery("user: email eq 'test'").getFullMongoQuery();
        expected = QUser.user.email.eq("test");

        compareMongoQueries(mongoDbUtils, actual, expected);

        actual = evaluateQuery("user: email contains 'test'").getFullMongoQuery();
        expected = QUser.user.email.contains("test");

        compareMongoQueries(mongoDbUtils, actual, expected);
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
        LocalDateTime localDateTime = LocalDateTime.of(2011, 12, 3, 10, 15, 30);

        // id comparison
        String actual = evaluateQuery(String.format("case: id eq '%s'", GENERIC_OBJECT_ID)).getFullElasticQuery();
        String expected = String.format("stringId:%s", GENERIC_OBJECT_ID);
        assert expected.equals(actual);

        // processId comparison
        actual = evaluateQuery("case: processId eq 'test'").getFullElasticQuery();
        expected = "processId:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: processId contains 'test'").getFullElasticQuery();
        expected = "processId:*test*";
        assert expected.equals(actual);

        // processIdentifier comparison
        actual = evaluateQuery("case: processIdentifier eq 'test'").getFullElasticQuery();
        expected = "processIdentifier:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: processIdentifier contains 'test'").getFullElasticQuery();
        expected = "processIdentifier:*test*";
        assert expected.equals(actual);

        // title comparison
        actual = evaluateQuery("case: title eq 'test'").getFullElasticQuery();
        expected = "title:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: title contains 'test'").getFullElasticQuery();
        expected = "title:*test*";
        assert expected.equals(actual);

        // creationDate comparison
        actual = evaluateQuery("case: creationDate eq 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("creationDateSortable:%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: creationDate lt 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("creationDateSortable:<%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: creationDate lte 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("creationDateSortable:<=%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: creationDate gt 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("creationDateSortable:>%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: creationDate gte 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("creationDateSortable:>=%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        // author comparison
        actual = evaluateQuery("case: author eq 'test'").getFullElasticQuery();
        expected = "author:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: author contains 'test'").getFullElasticQuery();
        expected = "author:*test*";
        assert expected.equals(actual);

        // places comparison
        actual = evaluateQuery("case: places.p1.marking eq 1").getFullElasticQuery();
        expected = "places.p1.marking:1";
        assert expected.equals(actual);

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

        // data value comparison
        actual = evaluateQuery("case: data.field1.value eq 'test'").getFullElasticQuery();
        expected = "dataSet.field1.textValue:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value contains 'test'").getFullElasticQuery();
        expected = "dataSet.field1.textValue:*test*";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value eq 1").getFullElasticQuery();
        expected = "dataSet.field1.numberValue:1";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value lt 1").getFullElasticQuery();
        expected = "dataSet.field1.numberValue:<1";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value lte 1").getFullElasticQuery();
        expected = "dataSet.field1.numberValue:<=1";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value gt 1").getFullElasticQuery();
        expected = "dataSet.field1.numberValue:>1";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value gte 1").getFullElasticQuery();
        expected = "dataSet.field1.numberValue:>=1";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value eq 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("dataSet.field1.timestampValue:%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value lt 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("dataSet.field1.timestampValue:<%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value lte 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("dataSet.field1.timestampValue:<=%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value gt 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("dataSet.field1.timestampValue:>%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value gte 2011-12-03T10:15:30").getFullElasticQuery();
        expected = String.format("dataSet.field1.timestampValue:>=%s", Timestamp.valueOf(localDateTime).getTime());
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value eq true").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:true";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.value eq false").getFullElasticQuery();
        expected = "dataSet.field1.booleanValue:false";
        assert expected.equals(actual);

        // data options comparison
        actual = evaluateQuery("case: data.field1.options eq 'test'").getFullElasticQuery();
        expected = "dataSet.field1.options:test";
        assert expected.equals(actual);

        actual = evaluateQuery("case: data.field1.options contains 'test'").getFullElasticQuery();
        expected = "dataSet.field1.options:*test*";
        assert expected.equals(actual);
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

        // string comparison
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier lt 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier lte 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier gt 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier gte 'test'"));

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

    private void compareMongoQueries(MongoDbUtils<?> mongoDbUtils, Predicate actual, Predicate expected) {
        Document actualDocument = mongoDbUtils.convertPredicateToDocument(actual);
        Document expectedDocument = mongoDbUtils.convertPredicateToDocument(expected);

        assert actualDocument.equals(expectedDocument);
    }
}
