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
import lombok.Setter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bson.types.ObjectId;
import org.bson.types.QObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.utils.SearchUtils.*;

public class QueryLangEvaluator extends QueryLangBaseListener {

    private final ParseTreeProperty<String> elasticQuery = new ParseTreeProperty<>();
    private final ParseTreeProperty<Predicate> mongoQuery = new ParseTreeProperty<>();

    @Getter
    private QueryType type;
    @Getter
    private Boolean multiple;
    @Getter
    @Setter
    private Boolean searchWithElastic = false;
    @Getter
    private Predicate fullMongoQuery;
    @Getter
    private String fullElasticQuery;
    @Getter
    private Pageable pageable;

    private int pageNumber = 0;
    private int pageSize = 20;
    private final List<Sort.Order> sortOrders = new ArrayList<>();

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

    private void processConditionGroup(ParseTree child, ParseTree current, Boolean not, Boolean parenthesis) {
        Predicate predicate = getMongoQuery(child);
        String elasticQuery = getElasticQuery(child);

        if (predicate != null) {
            predicate = not ? (predicate).not() : (predicate);
        }

        if (elasticQuery != null) {
            if (parenthesis) {
                elasticQuery = "(" + elasticQuery + ")";
            }

            if (not) {
                elasticQuery = "NOT " + elasticQuery;
            }
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
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
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
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
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
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
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
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
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
    public void exitProcessConditionGroupBasic(QueryLangParser.ProcessConditionGroupBasicContext ctx) {
        processConditionGroup(ctx.processCondition(), ctx, false, false);
    }

    @Override
    public void exitProcessConditionGroupParenthesis(QueryLangParser.ProcessConditionGroupParenthesisContext ctx) {
        processConditionGroup(ctx.processConditions(), ctx, ctx.NOT() != null, true);
    }

    @Override
    public void exitProcessCondition(QueryLangParser.ProcessConditionContext ctx) {
        processBasicExpression(ctx.processComparisons(), ctx);
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
    public void exitCaseConditionGroupBasic(QueryLangParser.CaseConditionGroupBasicContext ctx) {
        processConditionGroup(ctx.caseCondition(), ctx, false, false);
    }

    @Override
    public void exitCaseConditionGroupParenthesis(QueryLangParser.CaseConditionGroupParenthesisContext ctx) {
        processConditionGroup(ctx.caseConditions(), ctx, ctx.NOT() != null, true);
    }

    @Override
    public void exitCaseCondition(QueryLangParser.CaseConditionContext ctx) {
        processBasicExpression(ctx.caseComparisons(), ctx);
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
    public void exitTaskConditionGroupBasic(QueryLangParser.TaskConditionGroupBasicContext ctx) {
        processConditionGroup(ctx.taskCondition(), ctx, false, false);
    }

    @Override
    public void exitTaskConditionGroupParenthesis(QueryLangParser.TaskConditionGroupParenthesisContext ctx) {
        processConditionGroup(ctx.taskConditions(), ctx, ctx.NOT() != null, true);
    }

    @Override
    public void exitTaskCondition(QueryLangParser.TaskConditionContext ctx) {
        processBasicExpression(ctx.taskComparisons(), ctx);
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
    public void exitUserConditionGroupBasic(QueryLangParser.UserConditionGroupBasicContext ctx) {
        processConditionGroup(ctx.userCondition(), ctx, false, false);
    }

    @Override
    public void exitUserConditionGroupParenthesis(QueryLangParser.UserConditionGroupParenthesisContext ctx) {
        processConditionGroup(ctx.userConditions(), ctx, ctx.NOT() != null, true);
    }

    @Override
    public void exitUserCondition(QueryLangParser.UserConditionContext ctx) {
        processBasicExpression(ctx.userComparisons(), ctx);
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
        boolean not = ctx.objectIdComparison().NOT() != null;
        checkOp(ComparisonType.ID, op);
        ObjectId objectId = getObjectIdValue(ctx.objectIdComparison().STRING().getText());

        switch (type) {
            case PROCESS:
                qObjectId = QPetriNet.petriNet.id;
                break;
            case CASE:
                qObjectId = QCase.case$.id;
                setElasticQuery(ctx, buildElasticQuery("stringId", op.getType(), objectId.toString(), not));
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

        setMongoQuery(ctx, buildObjectIdPredicate(qObjectId, op.getType(), objectId, not));
    }

    @Override
    public void exitTitleBasic(QueryLangParser.TitleBasicContext ctx) {
        StringPath stringPath;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQuery("title", op.getType(), string, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitTitleList(QueryLangParser.TitleListContext ctx) {
        StringPath stringPath;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQueryInList("title", stringList, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitTitleRange(QueryLangParser.TitleRangeContext ctx) {
        StringPath stringPath;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQueryInRange("title", leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitIdentifierBasic(QueryLangParser.IdentifierBasicContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitIdentifierList(QueryLangParser.IdentifierListContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitIdentifierRange(QueryLangParser.IdentifierRangeContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitVersionBasic(QueryLangParser.VersionBasicContext ctx) {
        Token op = ctx.op;
        boolean not = ctx.NOT() != null;
        String versionString = ctx.VERSION_NUMBER().getText();

        setMongoQuery(ctx, buildVersionPredicate(op.getType(), versionString, not));
    }

    @Override
    public void exitVersionListCmp(QueryLangParser.VersionListCmpContext ctx) {
        boolean not = ctx.inListVersionComparison().NOT() != null;
        List<String> stringList = ctx.inListVersionComparison().versionList().VERSION_NUMBER().stream().map(TerminalNode::getText).collect(Collectors.toList());

        setMongoQuery(ctx, buildVersionPredicateInList(stringList, not));
    }

    @Override
    public void exitVersionRangeCmp(QueryLangParser.VersionRangeCmpContext ctx) {
        boolean not = ctx.inRangeVersionComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeVersionComparison().versionRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeVersionComparison().versionRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeVersionComparison().versionRange().VERSION_NUMBER(0).getText());
        String rightString = getStringValue(ctx.inRangeVersionComparison().versionRange().VERSION_NUMBER(1).getText());

        setMongoQuery(ctx, buildVersionPredicateInRange(leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitCdDateBasic(QueryLangParser.CdDateBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        Token op = ctx.dateComparison().op;
        boolean not = ctx.dateComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitCdDateTimeBasic(QueryLangParser.CdDateTimeBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        Token op = ctx.dateTimeComparison().op;
        boolean not = ctx.dateTimeComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitCdDateList(QueryLangParser.CdDateListContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        boolean not = ctx.inListDateComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                List<String> timestampStringList = stringDateList.stream().map(dateString -> {
                    LocalDateTime localDateTime = toDateTime(dateString);
                    return String.valueOf(Timestamp.valueOf(localDateTime).getTime());
                }).collect(Collectors.toList());
                setElasticQuery(ctx, buildElasticQueryInList("creationDateSortable", timestampStringList, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicateInList(dateTimePath, stringDateList, not));
    }

    @Override
    public void exitCdDateRange(QueryLangParser.CdDateRangeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        boolean not = ctx.inRangeDateComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        LocalDateTime leftDateTime;
        LocalDateTime rightDateTime;
        if (ctx.inRangeDateComparison().dateRange() != null) {
            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
        } else {
            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
        }


        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQueryInRange("creationDateSortable", String.valueOf(Timestamp.valueOf(leftDateTime).getTime()), leftEndpointOpen, String.valueOf(Timestamp.valueOf(rightDateTime).getTime()), rightEndpointOpen, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + type);
        }

        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitProcessIdComparison(QueryLangParser.ProcessIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.processId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitProcessIdObjIdComparison(QueryLangParser.ProcessIdObjIdComparisonContext ctx) {
        QObjectId qObjectId = QCase.case$.petriNetObjectId;
        Token op = ctx.objectIdComparison().op;
        boolean not = ctx.objectIdComparison().NOT() != null;
        ObjectId objectId = getObjectIdValue(ctx.objectIdComparison().STRING().getText());

        setMongoQuery(ctx, buildObjectIdPredicate(qObjectId, op.getType(), objectId, not));
        setElasticQuery(ctx, buildElasticQuery("processId", op.getType(), objectId.toString(), not));
    }

    @Override
    public void exitProcessIdentifierBasic(QueryLangParser.ProcessIdentifierBasicContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("processIdentifier", op.getType(), string, not));
    }

    @Override
    public void exitProcessIdentifierList(QueryLangParser.ProcessIdentifierListContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitProcessIdentifierRange(QueryLangParser.ProcessIdentifierRangeContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitAuthorComparison(QueryLangParser.AuthorComparisonContext ctx) {
        StringPath stringPath = QCase.case$.author.id;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("author", op.getType(), string, not));
    }

    @Override
    public void exitTransitionIdBasic(QueryLangParser.TransitionIdBasicContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitTransitionIdList(QueryLangParser.TransitionIdListContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitTransitionIdRange(QueryLangParser.TransitionIdRangeContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
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
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitCaseIdComparison(QueryLangParser.CaseIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.caseId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitLaDateBasic(QueryLangParser.LaDateBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        Token op = ctx.dateComparison().op;
        boolean not = ctx.dateComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLaDateTimeBasic(QueryLangParser.LaDateTimeBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        Token op = ctx.dateTimeComparison().op;
        boolean not = ctx.dateTimeComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLaDateList(QueryLangParser.LaDateListContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        boolean not = ctx.inListDateComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        setMongoQuery(ctx, buildDateTimePredicateInList(dateTimePath, stringDateList, not));
    }

    @Override
    public void exitLaDateRange(QueryLangParser.LaDateRangeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
        boolean not = ctx.inRangeDateComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        LocalDateTime leftDateTime;
        LocalDateTime rightDateTime;
        if (ctx.inRangeDateComparison().dateRange() != null) {
            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
        } else {
            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
        }

        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitLfDateBasic(QueryLangParser.LfDateBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        Token op = ctx.dateComparison().op;
        boolean not = ctx.dateComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLfDateTimeBasic(QueryLangParser.LfDateTimeBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        Token op = ctx.dateTimeComparison().op;
        boolean not = ctx.dateTimeComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLfDateList(QueryLangParser.LfDateListContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        boolean not = ctx.inListDateComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        setMongoQuery(ctx, buildDateTimePredicateInList(dateTimePath, stringDateList, not));
    }

    @Override
    public void exitLfDateRange(QueryLangParser.LfDateRangeContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
        boolean not = ctx.inRangeDateComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        LocalDateTime leftDateTime;
        LocalDateTime rightDateTime;
        if (ctx.inRangeDateComparison().dateRange() != null) {
            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
        } else {
            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
        }

        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitNameBasic(QueryLangParser.NameBasicContext ctx) {
        StringPath stringPath = QUser.user.name;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitNameList(QueryLangParser.NameListContext ctx) {
        StringPath stringPath = QUser.user.name;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitNameRange(QueryLangParser.NameRangeContext ctx) {
        StringPath stringPath = QUser.user.name;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitSurnameBasic(QueryLangParser.SurnameBasicContext ctx) {
        StringPath stringPath = QUser.user.surname;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitSurnameList(QueryLangParser.SurnameListContext ctx) {
        StringPath stringPath = QUser.user.surname;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitSurnameRange(QueryLangParser.SurnameRangeContext ctx) {
        StringPath stringPath = QUser.user.surname;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitEmailBasic(QueryLangParser.EmailBasicContext ctx) {
        StringPath stringPath = QUser.user.email;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitEmailList(QueryLangParser.EmailListContext ctx) {
        StringPath stringPath = QUser.user.email;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitEmailRange(QueryLangParser.EmailRangeContext ctx) {
        StringPath stringPath = QUser.user.email;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
    }

    @Override
    public void exitDataString(QueryLangParser.DataStringContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".textValue", op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataStringList(QueryLangParser.DataStringListContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("dataSet." + fieldId + ".textValue", stringList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataStringRange(QueryLangParser.DataStringRangeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".textValue", leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataNumber(QueryLangParser.DataNumberContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        boolean not = ctx.numberComparison().NOT() != null;
        String number = ctx.numberComparison().number.getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".numberValue", op.getType(), number, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataNumberList(QueryLangParser.DataNumberListContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inListNumberComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListNumberComparison().intList() != null ? ctx.inListNumberComparison().intList().INT() : ctx.inListNumberComparison().doubleList().DOUBLE();
        List<String> stringNumberList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("dataSet." + fieldId + ".numberValue", stringNumberList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataNumberRange(QueryLangParser.DataNumberRangeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inRangeNumberComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        String leftNumberAsString;
        String rightNumberAsString;
        if (ctx.inRangeNumberComparison().intRange() != null) {
            leftEndpointOpen = ctx.inRangeNumberComparison().intRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeNumberComparison().intRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftNumberAsString = ctx.inRangeNumberComparison().intRange().INT(0).getText();
            rightNumberAsString = ctx.inRangeNumberComparison().intRange().INT(1).getText();
        } else {
            leftEndpointOpen = ctx.inRangeNumberComparison().doubleRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeNumberComparison().doubleRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftNumberAsString = ctx.inRangeNumberComparison().doubleRange().DOUBLE(0).getText();
            rightNumberAsString = ctx.inRangeNumberComparison().doubleRange().DOUBLE(1).getText();
        }

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".numberValue", leftNumberAsString, leftEndpointOpen, rightNumberAsString, rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDate(QueryLangParser.DataDateContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.dateComparison().op;
        checkOp(ComparisonType.DATE, op);
        boolean not = ctx.dateComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDatetime(QueryLangParser.DataDatetimeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.dateTimeComparison().op;
        checkOp(ComparisonType.DATETIME, op);
        boolean not = ctx.dateTimeComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".timestampValue", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDateList(QueryLangParser.DataDateListContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inListDateComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME();
        List<String> stringNumberList = terminalNodeList.stream().map(TerminalNode::getText).map(dateAsString -> {
            LocalDateTime localDateTime = toDateTime(dateAsString);
            return String.valueOf(Timestamp.valueOf(localDateTime).getTime());
        }).collect(Collectors.toList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("dataSet." + fieldId + ".timestampValue", stringNumberList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDateRange(QueryLangParser.DataDateRangeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inRangeDateComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        LocalDateTime leftDateTime;
        LocalDateTime rightDateTime;
        if (ctx.inRangeDateComparison().dateRange() != null) {
            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
        } else {
            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
        }

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".timestampValue", String.valueOf(Timestamp.valueOf(leftDateTime).getTime()), leftEndpointOpen, String.valueOf(Timestamp.valueOf(rightDateTime).getTime()), rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataBoolean(QueryLangParser.DataBooleanContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.booleanComparison().op;
        checkOp(ComparisonType.BOOLEAN, op);
        boolean not = ctx.booleanComparison().NOT() != null;
        String booleanValue = ctx.booleanComparison().BOOLEAN().getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".booleanValue", op.getType(), booleanValue, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsBasic(QueryLangParser.DataOptionsBasicContext ctx) {
        String fieldId = ctx.dataOptions().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".options", op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsList(QueryLangParser.DataOptionsListContext ctx) {
        String fieldId = ctx.dataOptions().fieldId.getText();
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = ctx.inListStringComparison().stringList().STRING().stream().map(node -> getStringValue(node.getText())).collect(Collectors.toList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("dataSet." + fieldId + ".options", stringList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsRange(QueryLangParser.DataOptionsRangeContext ctx) {
        String fieldId = ctx.dataOptions().fieldId.getText();
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        String leftString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(0).getText());
        String rightString = getStringValue(ctx.inRangeStringComparison().stringRange().STRING(1).getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".options", leftString, leftEndpointOpen, rightString, rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitPlacesBasic(QueryLangParser.PlacesBasicContext ctx) {
        String placeId = ctx.places().placeId.getText();
        Token op = ctx.numberComparison().op;
        checkOp(ComparisonType.NUMBER, op);
        boolean not = ctx.numberComparison().NOT() != null;
        String numberValue = ctx.numberComparison().number.getText();

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("places." + placeId + ".marking", op.getType(), numberValue, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitPlacesList(QueryLangParser.PlacesListContext ctx) {
        String placeId = ctx.places().placeId.getText();
        boolean not = ctx.inListNumberComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListNumberComparison().intList() != null ? ctx.inListNumberComparison().intList().INT() : ctx.inListNumberComparison().doubleList().DOUBLE();
        List<String> stringNumberList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("places." + placeId + ".marking", stringNumberList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitPlacesRange(QueryLangParser.PlacesRangeContext ctx) {
        String placeId = ctx.places().placeId.getText();
        boolean not = ctx.inRangeNumberComparison().NOT() != null;
        boolean leftEndpointOpen;
        boolean rightEndpointOpen;
        String leftNumberAsString;
        String rightNumberAsString;
        if (ctx.inRangeNumberComparison().intRange() != null) {
            leftEndpointOpen = ctx.inRangeNumberComparison().intRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeNumberComparison().intRange().leftEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftNumberAsString = ctx.inRangeNumberComparison().intRange().INT(0).getText();
            rightNumberAsString = ctx.inRangeNumberComparison().intRange().INT(1).getText();
        } else {
            leftEndpointOpen = ctx.inRangeNumberComparison().doubleRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
            rightEndpointOpen = ctx.inRangeNumberComparison().doubleRange().leftEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
            leftNumberAsString = ctx.inRangeNumberComparison().doubleRange().DOUBLE(0).getText();
            rightNumberAsString = ctx.inRangeNumberComparison().doubleRange().DOUBLE(1).getText();
        }

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("places." + placeId + ".marking", leftNumberAsString, leftEndpointOpen, rightNumberAsString, rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksStateComparison(QueryLangParser.TasksStateComparisonContext ctx) {
        String taskId = ctx.tasksState().taskId.getText();
        Token op = ctx.op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.NOT() != null;
        State state = ctx.state.getType() == QueryLangParser.ENABLED ? State.ENABLED : State.DISABLED;

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".state", op.getType(), state.toString(), not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdComparison(QueryLangParser.TasksUserIdComparisonContext ctx) {
        String taskId = ctx.tasksUserId().taskId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.stringComparison().NOT() != null;
        String string = getStringValue(ctx.stringComparison().STRING().getText());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".userId", op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitPaging(QueryLangParser.PagingContext ctx) {
        pageNumber = Integer.parseInt(ctx.pageNum.getText());

        if (ctx.pageSize != null) {
            pageSize = Integer.parseInt(ctx.pageSize.getText());
        }
    }

    @Override
    public void exitCaseSorting(QueryLangParser.CaseSortingContext ctx) {
        ctx.caseAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop;
            if (searchWithElastic) {
                // todo NAE-1997: sorting by data value, options
                if (attrOrd.caseAttribute().places() != null) {
                    prop = "places." + attrOrd.caseAttribute().places().placeId.getText() + ".marking";
                } else if (attrOrd.caseAttribute().tasksState() != null) {
                    prop = "tasks." + attrOrd.caseAttribute().tasksState().taskId.getText() + ".state.keyword";
                } else if (attrOrd.caseAttribute().tasksUserId() != null) {
                    prop = "tasks." + attrOrd.caseAttribute().tasksUserId().taskId.getText() + ".userId.keyword";
                } else {
                    prop = caseAttrToSortPropElasticMapping.get(attrOrd.caseAttribute().getText().toLowerCase());
                }
            } else {
                prop = caseAttrToSortPropMapping.get(attrOrd.caseAttribute().getText().toLowerCase());
            }

            if (prop == null) {
                return;
            }
            sortOrders.add(new Sort.Order(dir, prop));
        });
    }

    @Override
    public void exitProcessSorting(QueryLangParser.ProcessSortingContext ctx) {
        ctx.processAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = processAttrToSortPropMapping.get(attrOrd.processAttribute().getText().toLowerCase());
            if (prop == null) {
                return;
            }
            sortOrders.add(new Sort.Order(dir, prop));
        });
    }

    @Override
    public void exitTaskSorting(QueryLangParser.TaskSortingContext ctx) {
        ctx.taskAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = taskAttrToSortPropMapping.get(attrOrd.taskAttribute().getText().toLowerCase());
            if (prop == null) {
                return;
            }
            sortOrders.add(new Sort.Order(dir, prop));
        });
    }

    @Override
    public void exitUserSorting(QueryLangParser.UserSortingContext ctx) {
        ctx.userAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = userAttrToSortPropMapping.get(attrOrd.userAttribute().getText().toLowerCase());
            if (prop == null) {
                return;
            }
            sortOrders.add(new Sort.Order(dir, prop));
        });
    }
}
