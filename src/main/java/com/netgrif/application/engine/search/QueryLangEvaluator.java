package com.netgrif.application.engine.search;

import com.netgrif.application.engine.search.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.search.antlr4.QueryLangParser;
import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.search.enums.ComparisonType;
import com.netgrif.application.engine.search.enums.QueryType;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.State;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.bson.types.ObjectId;
import org.bson.types.QObjectId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.SearchUtils.*;

public class QueryLangEvaluator extends QueryLangBaseListener {

    ParseTreeProperty<String> elasticQuery = new ParseTreeProperty<>();
    ParseTreeProperty<Predicate> mongoQuery = new ParseTreeProperty<>();

    @Getter
    private QueryType type;
    @Getter
    private Boolean multiple;
    @Getter
    private Predicate fullMongoQuery;
    @Getter
    private String fullElasticQuery;

    public void setElasticQuery(ParseTree node, String query) {
        elasticQuery.put(node, query);
    }

    public String getElasticQuery(ParseTree node) {
        return elasticQuery.get(node);
    }

    public void setMongoQuery(ParseTree node, Predicate predicate) {
        mongoQuery.put(node, predicate);
    }

    public Predicate getMongoQuery(ParseTree node) {
        return mongoQuery.get(node);
    }

    private void processBasicExpression(ParseTree child, ParseTree current) {
        setMongoQuery(current, getMongoQuery(child));
        setElasticQuery(current, getElasticQuery(child));
    }

    private void processOrExpression(List<ParseTree> children, ParseTree current) {
        List<Predicate> predicates = children.stream()
                .map(this::getMongoQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String elasticQuery = children.stream()
                .map(this::getElasticQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" OR "));

        if (!predicates.isEmpty()) {
            BooleanBuilder predicate = new BooleanBuilder();
            predicates.forEach(predicate::or);
            setMongoQuery(current, predicate);
        }
        setElasticQuery(current, elasticQuery.isBlank() ? null : elasticQuery);
    }

    private void processAndExpression(List<ParseTree> children, ParseTree current) {
        List<Predicate> predicates = children.stream()
                .map(this::getMongoQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String elasticQuery = children.stream()
                .map(this::getElasticQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" AND "));

        if (!predicates.isEmpty()) {
            BooleanBuilder predicate = new BooleanBuilder();
            predicates.forEach(predicate::and);
            setMongoQuery(current, predicate);
        }
        setElasticQuery(current, elasticQuery.isBlank() ? null : elasticQuery);
    }

    private void processConditionGroup(ParseTree child, ParseTree current, Boolean not) {
        Predicate predicate = getMongoQuery(child);
        String elasticQuery = getElasticQuery(child);

        if (predicate != null) {
            predicate = not ? (predicate).not() : (predicate);
        }

        if (elasticQuery != null) {
            elasticQuery = not ? "NOT (" + elasticQuery + ")" : "(" + elasticQuery + ")";
        }

        setMongoQuery(current, predicate);
        setElasticQuery(current, elasticQuery);
    }

    private void processCondition(ParseTree child, ParseTree current, Boolean not) {
        Predicate predicate = getMongoQuery(child);
        String elasticQuery = getElasticQuery(child);

        if (not) {
            predicate = predicate != null ? predicate.not() : null;
            elasticQuery = elasticQuery != null ? "NOT " + elasticQuery : null;
        }

        setMongoQuery(current, predicate);
        setElasticQuery(current, elasticQuery);
    }

    @Override
    public void enterProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        type = QueryType.PROCESS;
        multiple = ctx.resource.getType() == QueryLangParser.PROCESSES;
    }

    @Override
    public void exitProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        processBasicExpression(ctx.processConditions(), ctx);
        fullMongoQuery = getMongoQuery(ctx);
        fullElasticQuery = getElasticQuery(ctx);
    }

    @Override
    public void enterCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        type = QueryType.CASE;
        multiple = ctx.resource.getType() == QueryLangParser.CASES;
    }

    @Override
    public void exitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        processBasicExpression(ctx.caseConditions(), ctx);
        fullMongoQuery = getMongoQuery(ctx);
        fullElasticQuery = getElasticQuery(ctx);
    }

    @Override
    public void enterTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        type = QueryType.TASK;
        multiple = ctx.resource.getType() == QueryLangParser.TASKS;
    }

    @Override
    public void exitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        processBasicExpression(ctx.taskConditions(), ctx);
        fullMongoQuery = getMongoQuery(ctx);
        fullElasticQuery = getElasticQuery(ctx);
    }

    @Override
    public void enterUserQuery(QueryLangParser.UserQueryContext ctx) {
        type = QueryType.USER;
        multiple = ctx.resource.getType() == QueryLangParser.USERS;
    }

    @Override
    public void exitUserQuery(QueryLangParser.UserQueryContext ctx) {
        processBasicExpression(ctx.userConditions(), ctx);
        fullMongoQuery = getMongoQuery(ctx);
        fullElasticQuery = getElasticQuery(ctx);
    }

    @Override
    public void exitProcessConditions(QueryLangParser.ProcessConditionsContext ctx) {
        processBasicExpression(ctx.processOrExpression(), ctx);
    }

    @Override
    public void exitProcessOrExpression(QueryLangParser.ProcessOrExpressionContext ctx) {
        List<ParseTree> children = ctx.processAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processOrExpression(children, ctx);
    }

    @Override
    public void exitProcessAndExpression(QueryLangParser.ProcessAndExpressionContext ctx) {
        List<ParseTree> children = ctx.processConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processAndExpression(children, ctx);
    }

    @Override
    public void exitProcessConditionGroup(QueryLangParser.ProcessConditionGroupContext ctx) {
        processConditionGroup(ctx.processCondition() != null ? ctx.processCondition() : ctx.processConditions(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitProcessCondition(QueryLangParser.ProcessConditionContext ctx) {
        processCondition(ctx.processComparisons(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitCaseConditions(QueryLangParser.CaseConditionsContext ctx) {
        processBasicExpression(ctx.caseOrExpression(), ctx);
    }

    @Override
    public void exitCaseOrExpression(QueryLangParser.CaseOrExpressionContext ctx) {
        List<ParseTree> children = ctx.caseAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processOrExpression(children, ctx);
    }

    @Override
    public void exitCaseAndExpression(QueryLangParser.CaseAndExpressionContext ctx) {
        List<ParseTree> children = ctx.caseConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processAndExpression(children, ctx);
    }

    @Override
    public void exitCaseConditionGroup(QueryLangParser.CaseConditionGroupContext ctx) {
        processConditionGroup(ctx.caseCondition() != null ? ctx.caseCondition() : ctx.caseConditions(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitCaseCondition(QueryLangParser.CaseConditionContext ctx) {
        processCondition(ctx.caseComparisons(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitTaskConditions(QueryLangParser.TaskConditionsContext ctx) {
        processBasicExpression(ctx.taskOrExpression(), ctx);
    }

    @Override
    public void exitTaskOrExpression(QueryLangParser.TaskOrExpressionContext ctx) {
        List<ParseTree> children = ctx.taskAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processOrExpression(children, ctx);
    }

    @Override
    public void exitTaskAndExpression(QueryLangParser.TaskAndExpressionContext ctx) {
        List<ParseTree> children = ctx.taskConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processAndExpression(children, ctx);
    }

    @Override
    public void exitTaskConditionGroup(QueryLangParser.TaskConditionGroupContext ctx) {
        processConditionGroup(ctx.taskCondition() != null ? ctx.taskCondition() : ctx.taskConditions(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitTaskCondition(QueryLangParser.TaskConditionContext ctx) {
        processCondition(ctx.taskComparisons(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitUserConditions(QueryLangParser.UserConditionsContext ctx) {
        processBasicExpression(ctx.userOrExpression(), ctx);
    }

    @Override
    public void exitUserOrExpression(QueryLangParser.UserOrExpressionContext ctx) {
        List<ParseTree> children = ctx.userAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processOrExpression(children, ctx);
    }

    @Override
    public void exitUserAndExpression(QueryLangParser.UserAndExpressionContext ctx) {
        List<ParseTree> children = ctx.userConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processAndExpression(children, ctx);
    }

    @Override
    public void exitUserConditionGroup(QueryLangParser.UserConditionGroupContext ctx) {
        processConditionGroup(ctx.userCondition() != null ? ctx.userCondition() : ctx.userConditions(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitUserCondition(QueryLangParser.UserConditionContext ctx) {
        processCondition(ctx.userComparisons(), ctx, ctx.NOT() != null);
    }

    @Override
    public void exitProcessComparisons(QueryLangParser.ProcessComparisonsContext ctx) {
        processBasicExpression(ctx.children.get(0), ctx);
    }

    @Override
    public void exitCaseComparisons(QueryLangParser.CaseComparisonsContext ctx) {
        processBasicExpression(ctx.children.get(0), ctx);
    }

    @Override
    public void exitTaskComparisons(QueryLangParser.TaskComparisonsContext ctx) {
        processBasicExpression(ctx.children.get(0), ctx);
    }

    @Override
    public void exitUserComparisons(QueryLangParser.UserComparisonsContext ctx) {
        processBasicExpression(ctx.children.get(0), ctx);
    }

    @Override
    public void exitIdComparison(QueryLangParser.IdComparisonContext ctx) {
        QObjectId qObjectId;
        Token op = ctx.objectIdComparison().op;
        checkOp(ComparisonType.ID, op);
        ObjectId objectId = getObjectIdValue(ctx.objectIdComparison().STRING().getText());

        switch (type) {
            case PROCESS:
                qObjectId = QPetriNet.petriNet.id;
                break;
            case CASE:
                qObjectId = QCase.case$.id;
                setElasticQuery(ctx, buildElasticQuery("stringId", op, objectId.toString()));
                break;
            case TASK:
                qObjectId = QTask.task.id;
                break;
            case USER:
                qObjectId = QUser.user.id;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildObjectIdPredicate(qObjectId, op, objectId));
    }

    @Override
    public void exitTitleComparison(QueryLangParser.TitleComparisonContext ctx) {
        StringPath stringPath;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQuery("title", op, string));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitIdentifierComparison(QueryLangParser.IdentifierComparisonContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitVersionComparison(QueryLangParser.VersionComparisonContext ctx) {
        Token op = ctx.op;
        String versionString = ctx.VERSION_NUMBER().getText();

        setMongoQuery(ctx, buildVersionPredicate(op, versionString));
    }

    @Override
    public void exitCdDate(QueryLangParser.CdDateContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        Token op = ctx.dateComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op, String.valueOf(Timestamp.valueOf(localDateTime).getTime())));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitCdDateTime(QueryLangParser.CdDateTimeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        Token op = ctx.dateTimeComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op, String.valueOf(Timestamp.valueOf(localDateTime).getTime())));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitProcessIdComparison(QueryLangParser.ProcessIdComparisonContext ctx) {
        StringPath stringPath;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        switch (type) {
            case CASE:
                stringPath = QCase.case$.petriNetId;
                setElasticQuery(ctx, buildElasticQuery("processId", op, string));
                break;
            case TASK:
                stringPath = QTask.task.processId;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitProcessIdentifierComparison(QueryLangParser.ProcessIdentifierComparisonContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
        setElasticQuery(ctx, buildElasticQuery("processIdentifier", op, string));
    }

    @Override
    public void exitAuthorComparison(QueryLangParser.AuthorComparisonContext ctx) {
        StringPath stringPath = QCase.case$.author.id;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
        setElasticQuery(ctx, buildElasticQuery("author", op, string));
    }

    @Override
    public void exitTransitionIdComparison(QueryLangParser.TransitionIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitStateComparison(QueryLangParser.StateComparisonContext ctx) {
        switch (ctx.state.getType()) {
            case QueryLangParser.ENABLED:
                setMongoQuery(ctx, QTask.task.state.eq(State.ENABLED));
                break;
            case QueryLangParser.DISABLED:
                setMongoQuery(ctx, QTask.task.state.eq(State.DISABLED));
                break;
        }
    }

    @Override
    public void exitUserIdComparison(QueryLangParser.UserIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.userId;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitCaseIdComparison(QueryLangParser.CaseIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.caseId;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitLaDate(QueryLangParser.LaDateContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        Token op = ctx.dateComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitLaDateTime(QueryLangParser.LaDateTimeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        Token op = ctx.dateTimeComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitLfDate(QueryLangParser.LfDateContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        Token op = ctx.dateComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitLfDateTime(QueryLangParser.LfDateTimeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        Token op = ctx.dateTimeComparison().op;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op, localDateTime));
    }

    @Override
    public void exitNameComparison(QueryLangParser.NameComparisonContext ctx) {
        StringPath stringPath = QUser.user.name;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitSurnameComparison(QueryLangParser.SurnameComparisonContext ctx) {
        StringPath stringPath = QUser.user.surname;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitEmailComparison(QueryLangParser.EmailComparisonContext ctx) {
        StringPath stringPath = QUser.user.email;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
    }

    @Override
    public void exitDataString(QueryLangParser.DataStringContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".textValue", op, string));
    }

    @Override
    public void exitDataNumber(QueryLangParser.DataNumberContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        String number = ctx.numberComparison().NUMBER().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".numberValue", op, number));
    }

    @Override
    public void exitDataDate(QueryLangParser.DataDateContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.dateComparison().op;
        checkOp(ComparisonType.DATE, op);
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op, Timestamp.valueOf(localDateTime).toString()));
    }

    @Override
    public void exitDataDatetime(QueryLangParser.DataDatetimeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.dateTimeComparison().op;
        checkOp(ComparisonType.DATETIME, op);
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op, Timestamp.valueOf(localDateTime).toString()));
    }

    @Override
    public void exitDataBoolean(QueryLangParser.DataBooleanContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.booleanComparison().op;
        checkOp(ComparisonType.BOOLEAN, op);
        String booleanValue = ctx.booleanComparison().BOOLEAN().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".booleanValue", op, booleanValue));
    }

    @Override
    public void enterDataOptionsComparison(QueryLangParser.DataOptionsComparisonContext ctx) {
        String fieldId = ctx.dataOptions().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".options", op, string));
    }

    @Override
    public void exitPlacesComparison(QueryLangParser.PlacesComparisonContext ctx) {
        String placeId = ctx.places().placeId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        String numberValue = ctx.numberComparison().NUMBER().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("places." + placeId + ".marking", op, numberValue));
    }

    @Override
    public void exitTasksStateComparison(QueryLangParser.TasksStateComparisonContext ctx) {
        String taskId = ctx.tasksState().taskId.getText();
        Token op = ctx.op;
        checkOp(ComparisonType.STRING, op);
        State state = ctx.state.getType() == QueryLangParser.ENABLED ? State.ENABLED : State.DISABLED;

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".state", op, state.toString()));
    }

    @Override
    public void exitTasksUserIdComparison(QueryLangParser.TasksUserIdComparisonContext ctx) {
        String taskId = ctx.tasksUserId().taskId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".userId", op, string));
    }
}
