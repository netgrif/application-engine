package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.antlr4.QueryLangParser;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.SearchUtils.*;

public class QueryLangEvaluator extends QueryLangBaseListener {

    ParseTreeProperty<String> elasticQuery = new ParseTreeProperty<>();
    ParseTreeProperty<Predicate> mongoQuery = new ParseTreeProperty<>();

    @Getter
    private QueryType type;
    @Getter
    private Boolean multiple;

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
        List<Predicate> predicates = children.stream().map(this::getMongoQuery).collect(Collectors.toList());
        String elasticQuery = children.stream().map(this::getElasticQuery).collect(Collectors.joining(" OR "));

        if (predicates.contains(null)) {
            setMongoQuery(current, null);
        } else {
            BooleanBuilder predicate = new BooleanBuilder();
            predicates.forEach(predicate::or);
            setMongoQuery(current, predicate);
        }
        setElasticQuery(current, elasticQuery);
    }

    private void processAndExpression(List<ParseTree> children, ParseTree current) {
        List<Predicate> predicates = children.stream().map(this::getMongoQuery).collect(Collectors.toList());
        String elasticQuery = children.stream().map(this::getElasticQuery).collect(Collectors.joining(" AND "));

        if (predicates.contains(null)) {
            setMongoQuery(current, null);
        } else {
            BooleanBuilder predicate = new BooleanBuilder();
            predicates.forEach(predicate::and);
            setMongoQuery(current, predicate);
        }
        setElasticQuery(current, elasticQuery);
    }

    private void processConditionGroup(ParseTree child, ParseTree current) {
        Predicate predicate = getMongoQuery(child);
        String elasticQuery = getElasticQuery(child);

        setMongoQuery(current, (predicate));
        setElasticQuery(current, "(" + elasticQuery + ")");
    }

    private void processCondition(ParseTree child, ParseTree current, Boolean not) {
        Predicate predicate = getMongoQuery(child);
        String elasticQuery = getElasticQuery(child);

        if (not) {
            predicate = predicate != null ? predicate.not() : null;
            elasticQuery = "NOT " + elasticQuery;
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
    }

    @Override
    public void enterCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        type = QueryType.CASE;
        multiple = ctx.resource.getType() == QueryLangParser.CASES;
    }

    @Override
    public void exitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        processBasicExpression(ctx.caseConditions(), ctx);
    }

    @Override
    public void enterTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        type = QueryType.TASK;
        multiple = ctx.resource.getType() == QueryLangParser.TASKS;
    }

    @Override
    public void exitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        processBasicExpression(ctx.taskConditions(), ctx);
    }

    @Override
    public void enterUserQuery(QueryLangParser.UserQueryContext ctx) {
        type = QueryType.USER;
        multiple = ctx.resource.getType() == QueryLangParser.USERS;
    }

    @Override
    public void exitUserQuery(QueryLangParser.UserQueryContext ctx) {
        processBasicExpression(ctx.userConditions(), ctx);
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
        processConditionGroup(ctx.processCondition() != null ? ctx.processCondition() : ctx.processConditions(), ctx);
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
        processConditionGroup(ctx.caseCondition() != null ? ctx.caseCondition() : ctx.caseConditions(), ctx);
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
        processConditionGroup(ctx.taskCondition() != null ? ctx.taskCondition() : ctx.taskConditions(), ctx);
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
        processConditionGroup(ctx.userCondition() != null ? ctx.userCondition() : ctx.userConditions(), ctx);
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
        StringPath stringPath;
        Token op = ctx.stringComparison().op;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.stringId;
                break;
            case CASE:
                stringPath = QCase.case$.stringId;
                setElasticQuery(ctx, buildElasticQuery("stringId", op, string));
                break;
            case TASK:
                stringPath = QTask.task.stringId;
                break;
            case USER:
                stringPath = QUser.user.stringId;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicate(stringPath, op, string));
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
            case QueryLangParser.DISABLED:
                setMongoQuery(ctx, QTask.task.state.eq(State.DISABLED));
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
    public void exitDataString(QueryLangParser.DataStringContext ctx) { // todo NAE-1997: options comparison grammar update??
        String fieldId = ctx.data().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".textValue", op, string));
    }

    @Override
    public void exitDataNumber(QueryLangParser.DataNumberContext ctx) {
        if (ctx.data().property.getType() == QueryLangParser.OPTIONS) {
            throw new IllegalArgumentException("Search by number value is not applicable for options.");
        }

        String fieldId = ctx.data().fieldId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        String number = ctx.numberComparison().NUMBER().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".numberValue", op, number));
    }

    @Override
    public void exitDataDate(QueryLangParser.DataDateContext ctx) {
        if (ctx.data().property.getType() == QueryLangParser.OPTIONS) {
            throw new IllegalArgumentException("Search by date value is not applicable for options.");
        }

        String fieldId = ctx.data().fieldId.getText();
        Token op = ctx.dateComparison().op;
        checkOp(ComparisonType.DATE, op);
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op, Timestamp.valueOf(localDateTime).toString()));
    }

    @Override
    public void exitDataDatetime(QueryLangParser.DataDatetimeContext ctx) {
        if (ctx.data().property.getType() == QueryLangParser.OPTIONS) {
            throw new IllegalArgumentException("Search by date time value is not applicable for options.");
        }

        String fieldId = ctx.data().fieldId.getText();
        Token op = ctx.dateTimeComparison().op;
        checkOp(ComparisonType.DATETIME, op);
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op, Timestamp.valueOf(localDateTime).toString()));
    }

    @Override
    public void exitDataBoolean(QueryLangParser.DataBooleanContext ctx) {
        if (ctx.data().property.getType() == QueryLangParser.OPTIONS) {
            throw new IllegalArgumentException("Search by boolean value is not applicable for options.");
        }

        String fieldId = ctx.data().fieldId.getText();
        Token op = ctx.booleanComparison().op;
        checkOp(ComparisonType.BOOLEAN, op);
        String booleanValue = ctx.booleanComparison().BOOLEAN().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".booleanValue", op, Timestamp.valueOf(booleanValue).toString()));
    }

    @Override
    public void exitPlacesComparison(QueryLangParser.PlacesComparisonContext ctx) {
        String placeId = ctx.places().placeId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        String numberValue = ctx.numberComparison().NUMBER().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("places." + placeId + ".marking", op, numberValue)); // todo NAE-1997: update places structure in elastic/another approach?
    }

    @Override
    public void exitTasksComparison(QueryLangParser.TasksComparisonContext ctx) {
        String taskId = ctx.tasks().taskId.getText();
        String property = ctx.tasks().property.getType() == QueryLangParser.STATE ? ".state" : "userId";
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + property, op, string)); // todo NAE-1997: update tasks structure in elastic/another approach?
    }
}
