package com.netgrif.application.engine.pfql.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.pfql.domain.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.pfql.domain.antlr4.QueryLangParser;
import com.netgrif.application.engine.pfql.domain.enums.ComparisonType;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
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
import org.springframework.data.util.Pair;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.*;

public class QueryLangEvaluator extends QueryLangBaseListener {

    private final ParseTreeProperty<String> elasticQuery = new ParseTreeProperty<>();
    private final ParseTreeProperty<Predicate> mongoQuery = new ParseTreeProperty<>();
    private final String elasticFuzzyMaxDistance = "2";

    private final IUserService userService;

    @Getter
    private QueryType resourceType;
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
    @Setter
    private Pageable pageable;

    private int pageNumber = 0;
    private int pageSize = 20;
    private final List<Sort.Order> sortOrders = new ArrayList<>();

    public QueryLangEvaluator(IUserService userService) {
        this.userService = userService;
    }

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

    private String handleStringComparisonWithPlaceholders(QueryLangParser.StringComparisonContext ctx) {
        if (ctx.STRING() != null) {
            return getStringValue(ctx.STRING().getText());
        }

        if (ctx.loggedUserStringAttribute() != null) {
            return handleLoggedUserStringAttribute(ctx.loggedUserStringAttribute());
        }

        throw new IllegalArgumentException("Wrong or missing query value on string comparison");
    }

    private ObjectId handleObjectIdComparisonWithPlaceholders(QueryLangParser.ObjectIdComparisonContext ctx) {
        if (ctx.STRING() != null) {
            return getObjectIdValue(ctx.STRING().getText());
        }
        if (ctx.LOGGED_USER_ID() != null) {
            LoggedUser loggedUser = this.userService.getLoggedOrSystem().transformToLoggedUser();
            return getObjectIdValue(loggedUser.getId());
        }

        throw new IllegalArgumentException("Wrong or missing query value on object id comparison");
    }

    private List<String> handleStringListComparison(QueryLangParser.StringListContext ctx) {
        List<String> result = new ArrayList<>();

        if (ctx.STRING() != null && !ctx.STRING().isEmpty()) {
            result.addAll(ctx.STRING().stream()
                    .map(node -> getStringValue(node.getText()))
                    .collect(Collectors.toList()));
        }
        if (ctx.loggedUserStringAttribute() != null && !ctx.loggedUserStringAttribute().isEmpty()) {
            result.addAll(ctx.loggedUserStringAttribute().stream()
                    .map(this::handleLoggedUserStringAttribute)
                    .collect(Collectors.toList()));
        }

        return result;
    }

    private List<ObjectId> handleObjectIdListComparison(QueryLangParser.StringListContext ctx) {
        List<ObjectId> result = new ArrayList<>();

        if (ctx.STRING() != null && !ctx.STRING().isEmpty()) {
            result.addAll(ctx.STRING().stream()
                    .map(node -> getObjectIdValue(node.getText()))
                    .collect(Collectors.toList()));
        }
        if (ctx.loggedUserStringAttribute() != null && !ctx.loggedUserStringAttribute().isEmpty()) {
            boolean hasIdAttribute = ctx.loggedUserStringAttribute().stream()
                    .anyMatch(loggedUserCtx -> loggedUserCtx.LOGGED_USER_ID() != null);
            if (hasIdAttribute) {
                LoggedUser loggedUser = this.userService.getLoggedOrSystem().transformToLoggedUser();
                result.add(getObjectIdValue(loggedUser.getId()));
            }
        }

        return result;
    }

    private String handleLoggedUserStringAttribute(QueryLangParser.LoggedUserStringAttributeContext ctx) {
        LoggedUser loggedUser = this.userService.getLoggedOrSystem().transformToLoggedUser();
        if (ctx.LOGGED_USER_ID() != null) {
            return loggedUser.getId();
        }
        if (ctx.LOGGED_USER_USERNAME() != null) {
            return loggedUser.getUsername();
        }
        if (ctx.LOGGED_USER_FULLNAME() != null) {
            return loggedUser.getFullName();
        }
        return "";
    }

    /// returns pair, where the first element is left value and the second element is right value
    private Pair<String, String> handleInRangeStringComparison(QueryLangParser.StringRangeContext ctx) {
        List<ParseTree> filteredChildren = ctx.children.stream()
                .filter(node -> node instanceof TerminalNode && ((TerminalNode) node).getSymbol().getType() == QueryLangParser.STRING
                        || node instanceof QueryLangParser.LoggedUserStringAttributeContext)
                .collect(Collectors.toList());

        if (filteredChildren.size() < 2) {
            throw new IllegalArgumentException("Wrong in range values");
        }

        String left;
        ParseTree leftNode = filteredChildren.get(0);
        if (leftNode instanceof TerminalNode) {
            left = getStringValue(leftNode.getText());
        } else {
            left = handleLoggedUserStringAttribute((QueryLangParser.LoggedUserStringAttributeContext) leftNode);
        }

        String right;
        ParseTree rightNode = filteredChildren.get(1);
        if (rightNode instanceof TerminalNode) {
            right = getStringValue(rightNode.getText());
        } else {
            right = handleLoggedUserStringAttribute((QueryLangParser.LoggedUserStringAttributeContext) rightNode);
        }

        return Pair.of(left, right);
    }

    private String handleBooleanComparison(QueryLangParser.BooleanComparisonContext ctx) {
        if (ctx.BOOLEAN() != null) {
            return ctx.BOOLEAN().getText();
        }
        if (ctx.LOGGED_USER_ANONYMOUS() != null) {
            LoggedUser loggedUser = this.userService.getLoggedOrSystem().transformToLoggedUser();
            return String.valueOf(loggedUser.isAnonymous());
        }

        throw new IllegalArgumentException("Wrong or missing query value on boolean comparison");
    }

    private Predicate getEmptyMongoQuery() {
        return new BooleanBuilder();
    }

    private String getEmptyElasticQuery() {
        return "*";
    }

    private void handleNoneConditions() {
        fullMongoQuery = getEmptyMongoQuery();
        fullElasticQuery = getEmptyElasticQuery();
    }

    @Override
    public void enterProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        resourceType = QueryType.PROCESS;
        multiple = ctx.resource.getType() == QueryLangParser.PROCESSES;
    }

    @Override
    public void exitProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        if (ctx.processConditionsAndPaging() == null || ctx.processConditionsAndPaging().processConditions() == null) {
            handleNoneConditions();
        } else {
            processBasicExpression(ctx.processConditionsAndPaging().processConditions(), ctx);
            fullMongoQuery = getMongoQuery(ctx);
            fullElasticQuery = getElasticQuery(ctx);
        }
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
    }

    @Override
    public void enterCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        resourceType = QueryType.CASE;
        multiple = ctx.resource.getType() == QueryLangParser.CASES;
    }

    @Override
    public void exitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        if (ctx.caseConditionsAndPaging() == null || ctx.caseConditionsAndPaging().caseConditions() == null) {
            handleNoneConditions();
        } else {
            processBasicExpression(ctx.caseConditionsAndPaging().caseConditions(), ctx);
            fullMongoQuery = getMongoQuery(ctx);
            fullElasticQuery = getElasticQuery(ctx);
        }
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
    }

    @Override
    public void enterTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        resourceType = QueryType.TASK;
        multiple = ctx.resource.getType() == QueryLangParser.TASKS;
    }

    @Override
    public void exitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        if (ctx.taskConditionsAndPaging() == null || ctx.taskConditionsAndPaging().taskConditions() == null) {
            handleNoneConditions();
        } else {
            processBasicExpression(ctx.taskConditionsAndPaging().taskConditions(), ctx);
            fullMongoQuery = getMongoQuery(ctx);
            fullElasticQuery = getElasticQuery(ctx);
        }
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
    }

    @Override
    public void enterUserQuery(QueryLangParser.UserQueryContext ctx) {
        resourceType = QueryType.USER;
        multiple = ctx.resource.getType() == QueryLangParser.USERS;
    }

    @Override
    public void exitUserQuery(QueryLangParser.UserQueryContext ctx) {
        if (ctx.userConditionsAndPaging() == null || ctx.userConditionsAndPaging().userConditions() == null) {
            handleNoneConditions();
        } else {
            processBasicExpression(ctx.userConditionsAndPaging().userConditions(), ctx);
            fullMongoQuery = getMongoQuery(ctx);
            fullElasticQuery = getElasticQuery(ctx);
        }
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
    public void exitIdBasic(QueryLangParser.IdBasicContext ctx) {
        QObjectId qObjectId;
        Token op = ctx.objectIdComparison().op;
        boolean not = ctx.objectIdComparison().NOT() != null;
        checkOp(ComparisonType.ID, op);
        ObjectId objectId = handleObjectIdComparisonWithPlaceholders(ctx.objectIdComparison());

        switch (resourceType) {
            case PROCESS:
                qObjectId = QPetriNet.petriNet._id;
                break;
            case CASE:
                qObjectId = QCase.case$._id;
                setElasticQuery(ctx, buildElasticQuery("stringId", op.getType(), objectId.toString(), not));
                break;
            case TASK:
                qObjectId = QTask.task._id;
                setElasticQuery(ctx, buildElasticQuery("stringId", op.getType(), objectId.toString(), not));
                break;
            case USER:
                qObjectId = QUser.user._id;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildObjectIdPredicate(qObjectId, op.getType(), objectId, not));
    }

    @Override
    public void exitIdList(QueryLangParser.IdListContext ctx) {
        QObjectId qObjectId;
        Token op = ctx.inListStringComparison().op;
        boolean not = ctx.inListStringComparison().NOT() != null;
        checkOp(ComparisonType.ID, op);
        List<ObjectId> objectIdList = handleObjectIdListComparison(ctx.inListStringComparison().stringList());
        List<String> stringIdList = objectIdList.stream().map(ObjectId::toString).collect(Collectors.toList());

        switch (resourceType) {
            case PROCESS:
                qObjectId = QPetriNet.petriNet._id;
                break;
            case CASE:
                qObjectId = QCase.case$._id;
                setElasticQuery(ctx, buildElasticQueryInList("stringId", stringIdList, not));
                break;
            case TASK:
                qObjectId = QTask.task._id;
                setElasticQuery(ctx, buildElasticQueryInList("stringId", stringIdList, not));
                break;
            case USER:
                qObjectId = QUser.user._id;
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildObjectIdPredicateInList(qObjectId, objectIdList, not));
    }

    @Override
    public void exitTitleBasic(QueryLangParser.TitleBasicContext ctx) {
        StringPath stringPath;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());
        String elasticAttribute = "title";
        if (op.getType() == QueryLangParser.EQ || op.getType() == QueryLangParser.NEQ) {
            elasticAttribute += ".keyword";
        }

        switch (resourceType) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQuery(elasticAttribute, op.getType(), string, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                setElasticQuery(ctx, buildElasticQuery(elasticAttribute, op.getType(), string, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitTitleList(QueryLangParser.TitleListContext ctx) {
        StringPath stringPath;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        switch (resourceType) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQueryInList("title", stringList, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                setElasticQuery(ctx, buildElasticQueryInList("title", stringList, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitTitleRange(QueryLangParser.TitleRangeContext ctx) {
        StringPath stringPath;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        switch (resourceType) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQueryInRange("title", leftAndRightStrings.getFirst(),
                        leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                setElasticQuery(ctx, buildElasticQueryInRange("title", leftAndRightStrings.getFirst(),
                        leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitIdentifierBasic(QueryLangParser.IdentifierBasicContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitIdentifierList(QueryLangParser.IdentifierListContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitIdentifierRange(QueryLangParser.IdentifierRangeContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
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

        switch (resourceType) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitCdDateTimeBasic(QueryLangParser.CdDateTimeBasicContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        Token op = ctx.dateTimeComparison().op;
        boolean not = ctx.dateTimeComparison().NOT() != null;
        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());

        switch (resourceType) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQuery("creationDateSortable", op.getType(), String.valueOf(Timestamp.valueOf(localDateTime).getTime()), not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitCdDateList(QueryLangParser.CdDateListContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        boolean not = ctx.inListDateComparison().NOT() != null;
        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());

        switch (resourceType) {
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
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
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


        switch (resourceType) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                setElasticQuery(ctx, buildElasticQueryInRange("creationDateSortable", String.valueOf(Timestamp.valueOf(leftDateTime).getTime()), leftEndpointOpen, String.valueOf(Timestamp.valueOf(rightDateTime).getTime()), rightEndpointOpen, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitProcessIdBasic(QueryLangParser.ProcessIdBasicContext ctx) {
        StringPath stringPath = QTask.task.processId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("processId", op.getType(), string, not));
    }

    @Override
    public void exitProcessIdList(QueryLangParser.ProcessIdListContext ctx) {
        StringPath stringPath = QTask.task.processId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("processId", stringList, not));
    }

    @Override
    public void exitProcessIdObjIdBasic(QueryLangParser.ProcessIdObjIdBasicContext ctx) {
        QObjectId qObjectId = QCase.case$.petriNetObjectId;
        Token op = ctx.objectIdComparison().op;
        boolean not = ctx.objectIdComparison().NOT() != null;
        ObjectId objectId = handleObjectIdComparisonWithPlaceholders(ctx.objectIdComparison());

        setMongoQuery(ctx, buildObjectIdPredicate(qObjectId, op.getType(), objectId, not));
        setElasticQuery(ctx, buildElasticQuery("processId", op.getType(), objectId.toString(), not));
    }

    @Override
    public void exitProcessIdObjIdList(QueryLangParser.ProcessIdObjIdListContext ctx) {
        QObjectId qObjectId = QCase.case$.petriNetObjectId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<ObjectId> objectIdList = handleObjectIdListComparison(ctx.inListStringComparison().stringList());
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildObjectIdPredicateInList(qObjectId, objectIdList, not));
        setElasticQuery(ctx, buildElasticQueryInList("processId", stringList, not));
    }

    @Override
    public void exitProcessIdentifierBasic(QueryLangParser.ProcessIdentifierBasicContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("processIdentifier", op.getType(), string, not));
    }

    @Override
    public void exitProcessIdentifierList(QueryLangParser.ProcessIdentifierListContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("processIdentifier", stringList, not));
    }

    @Override
    public void exitProcessIdentifierRange(QueryLangParser.ProcessIdentifierRangeContext ctx) {
        StringPath stringPath = QCase.case$.processIdentifier;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
        setElasticQuery(ctx, buildElasticQueryInRange("processIdentifier", leftAndRightStrings.getFirst(),
                leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitAuthorBasic(QueryLangParser.AuthorBasicContext ctx) {
        StringPath stringPath = QCase.case$.author.id;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("author", op.getType(), string, not));
    }

    @Override
    public void exitAuthorList(QueryLangParser.AuthorListContext ctx) {
        StringPath stringPath = QCase.case$.author.id;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("author", stringList, not));
    }

    @Override
    public void exitTransitionIdBasic(QueryLangParser.TransitionIdBasicContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("transitionId", op.getType(), string, not));
    }

    @Override
    public void exitTransitionIdList(QueryLangParser.TransitionIdListContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("transitionId", stringList, not));
    }

    @Override
    public void exitTransitionIdRange(QueryLangParser.TransitionIdRangeContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
        setElasticQuery(ctx, buildElasticQueryInRange("transitionId", leftAndRightStrings.getFirst(),
                leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitStateComparison(QueryLangParser.StateComparisonContext ctx) {
        // todo implement task states
//        switch (ctx.state.getType()) {
//            case QueryLangParser.ENABLED:
//                setMongoQuery(ctx, QTask.task.state.eq(State.ENABLED));
//                break;
//            case QueryLangParser.DISABLED:
//                setMongoQuery(ctx, QTask.task.state.eq(State.DISABLED));
//                break;
//        }
    }

    @Override
    public void exitUserIdBasic(QueryLangParser.UserIdBasicContext ctx) {
        StringPath stringPath = QTask.task.userId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("userId", op.getType(), string, not));
    }

    @Override
    public void exitUserIdList(QueryLangParser.UserIdListContext ctx) {
        StringPath stringPath = QTask.task.userId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("userId", stringList, not));
    }

    @Override
    public void exitCaseIdBasic(QueryLangParser.CaseIdBasicContext ctx) {
        StringPath stringPath = QTask.task.caseId;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
        setElasticQuery(ctx, buildElasticQuery("caseId", op.getType(), string, not));
    }

    @Override
    public void exitCaseIdList(QueryLangParser.CaseIdListContext ctx) {
        StringPath stringPath = QTask.task.caseId;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
        setElasticQuery(ctx, buildElasticQueryInList("caseId", stringList, not));
    }

    @Override
    public void exitLaDateBasic(QueryLangParser.LaDateBasicContext ctx) {
        // todo implement lastAssigned
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
//        Token op = ctx.dateComparison().op;
//        boolean not = ctx.dateComparison().NOT() != null;
//        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());
//
//        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLaDateTimeBasic(QueryLangParser.LaDateTimeBasicContext ctx) {
        // todo implement lastAssigned
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
//        Token op = ctx.dateTimeComparison().op;
//        boolean not = ctx.dateTimeComparison().NOT() != null;
//        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());
//
//        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLaDateList(QueryLangParser.LaDateListContext ctx) {
        // todo implement lastAssigned
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
//        boolean not = ctx.inListDateComparison().NOT() != null;
//        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
//        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());
//
//        setMongoQuery(ctx, buildDateTimePredicateInList(dateTimePath, stringDateList, not));
    }

    @Override
    public void exitLaDateRange(QueryLangParser.LaDateRangeContext ctx) {
        // todo implement lastAssigned
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;
//        boolean not = ctx.inRangeDateComparison().NOT() != null;
//        boolean leftEndpointOpen;
//        boolean rightEndpointOpen;
//        LocalDateTime leftDateTime;
//        LocalDateTime rightDateTime;
//        if (ctx.inRangeDateComparison().dateRange() != null) {
//            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
//            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
//            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
//            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
//        } else {
//            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
//            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
//            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
//            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
//        }
//
//        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitLfDateBasic(QueryLangParser.LfDateBasicContext ctx) {
        // todo implement lastFinished
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
//        Token op = ctx.dateComparison().op;
//        boolean not = ctx.dateComparison().NOT() != null;
//        LocalDateTime localDateTime = toDateTime(ctx.dateComparison().DATE().getText());
//
//        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLfDateTimeBasic(QueryLangParser.LfDateTimeBasicContext ctx) {
        // todo implement lastFinished
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
//        Token op = ctx.dateTimeComparison().op;
//        boolean not = ctx.dateTimeComparison().NOT() != null;
//        LocalDateTime localDateTime = toDateTime(ctx.dateTimeComparison().DATETIME().getText());
//
//        setMongoQuery(ctx, buildDateTimePredicate(dateTimePath, op.getType(), localDateTime, not));
    }

    @Override
    public void exitLfDateList(QueryLangParser.LfDateListContext ctx) {
        // todo implement lastFinished
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
//        boolean not = ctx.inListDateComparison().NOT() != null;
//        List<TerminalNode> terminalNodeList = ctx.inListDateComparison().dateList() != null ? ctx.inListDateComparison().dateList().DATE() : ctx.inListDateComparison().dateTimeList().DATETIME() ;
//        List<String> stringDateList = terminalNodeList.stream().map(TerminalNode::getText).collect(Collectors.toList());
//
//        setMongoQuery(ctx, buildDateTimePredicateInList(dateTimePath, stringDateList, not));
    }

    @Override
    public void exitLfDateRange(QueryLangParser.LfDateRangeContext ctx) {
        // todo implement lastFinished
//        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;
//        boolean not = ctx.inRangeDateComparison().NOT() != null;
//        boolean leftEndpointOpen;
//        boolean rightEndpointOpen;
//        LocalDateTime leftDateTime;
//        LocalDateTime rightDateTime;
//        if (ctx.inRangeDateComparison().dateRange() != null) {
//            leftEndpointOpen = ctx.inRangeDateComparison().dateRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
//            rightEndpointOpen = ctx.inRangeDateComparison().dateRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
//            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(0).getText());
//            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateRange().DATE(1).getText());
//        } else {
//            leftEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
//            rightEndpointOpen = ctx.inRangeDateComparison().dateTimeRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
//            leftDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(0).getText());
//            rightDateTime = toDateTime(ctx.inRangeDateComparison().dateTimeRange().DATETIME(1).getText());
//        }
//
//        setMongoQuery(ctx, buildDateTimePredicateInRange(dateTimePath, leftDateTime, leftEndpointOpen, rightDateTime, rightEndpointOpen, not));
    }

    @Override
    public void exitNameBasic(QueryLangParser.NameBasicContext ctx) {
        StringPath stringPath = QUser.user.name;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitNameList(QueryLangParser.NameListContext ctx) {
        StringPath stringPath = QUser.user.name;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitNameRange(QueryLangParser.NameRangeContext ctx) {
        StringPath stringPath = QUser.user.name;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitSurnameBasic(QueryLangParser.SurnameBasicContext ctx) {
        StringPath stringPath = QUser.user.surname;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitSurnameList(QueryLangParser.SurnameListContext ctx) {
        StringPath stringPath = QUser.user.surname;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitSurnameRange(QueryLangParser.SurnameRangeContext ctx) {
        StringPath stringPath = QUser.user.surname;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitEmailBasic(QueryLangParser.EmailBasicContext ctx) {
        StringPath stringPath = QUser.user.email;
        Token op = ctx.stringComparison().op;
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, buildStringPredicate(stringPath, op.getType(), string, not));
    }

    @Override
    public void exitEmailList(QueryLangParser.EmailListContext ctx) {
        StringPath stringPath = QUser.user.email;
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, buildStringPredicateInList(stringPath, stringList, not));
    }

    @Override
    public void exitEmailRange(QueryLangParser.EmailRangeContext ctx) {
        StringPath stringPath = QUser.user.email;
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, buildStringPredicateInRange(stringPath, leftAndRightStrings.getFirst(), leftEndpointOpen,
                leftAndRightStrings.getSecond(), rightEndpointOpen, not));
    }

    @Override
    public void exitDataString(QueryLangParser.DataStringContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, null);
        String attribute = "dataSet." + fieldId + ".fulltextValue";
        if (op.getType() == QueryLangParser.EQ || op.getType() == QueryLangParser.NEQ) {
            attribute += ".keyword";
        }
        setElasticQuery(ctx, buildElasticQuery(attribute, op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataStringList(QueryLangParser.DataStringListContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("dataSet." + fieldId + ".fulltextValue", stringList, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataStringRange(QueryLangParser.DataStringRangeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        boolean not = ctx.inRangeStringComparison().NOT() != null;
        boolean leftEndpointOpen = ctx.inRangeStringComparison().stringRange().leftEndpoint.getText().equals(LEFT_OPEN_ENDPOINT);
        boolean rightEndpointOpen = ctx.inRangeStringComparison().stringRange().rightEndpoint.getText().equals(RIGHT_OPEN_ENDPOINT);
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".fulltextValue", leftAndRightStrings.getFirst(),
                leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
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
        String booleanValue = handleBooleanComparison(ctx.booleanComparison());

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
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".options", op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsList(QueryLangParser.DataOptionsListContext ctx) {
        String fieldId = ctx.dataOptions().fieldId.getText();
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

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
        Pair<String, String> leftAndRightStrings = handleInRangeStringComparison(ctx.inRangeStringComparison().stringRange());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInRange("dataSet." + fieldId + ".options", leftAndRightStrings.getFirst(),
                leftEndpointOpen, leftAndRightStrings.getSecond(), rightEndpointOpen, not));
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
        setElasticQuery(ctx, buildElasticQueryInRange("places." + placeId + ".marking", leftNumberAsString,
                leftEndpointOpen, rightNumberAsString, rightEndpointOpen, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksStateComparison(QueryLangParser.TasksStateComparisonContext ctx) {
        // todo implement task states
//        String taskId = ctx.tasksState().taskId.getText();
//        Token op = ctx.op;
//        checkOp(ComparisonType.STRING, op);
//        boolean not = ctx.NOT() != null;
//        State state = ctx.state.getType() == QueryLangParser.ENABLED ? State.ENABLED : State.DISABLED;
//
//        setMongoQuery(ctx, null);
//        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".state", op.getType(), state.toString(), not));
//        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdBasic(QueryLangParser.TasksUserIdBasicContext ctx) {
        String taskId = ctx.tasksUserId().taskId.getText();
        Token op = ctx.stringComparison().op;
        checkOp(ComparisonType.STRING, op);
        boolean not = ctx.stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringComparison());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("tasks." + taskId + ".userId", op.getType(), string, not));
        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdList(QueryLangParser.TasksUserIdListContext ctx) {
        String taskId = ctx.tasksUserId().taskId.getText();
        boolean not = ctx.inListStringComparison().NOT() != null;
        List<String> stringList = handleStringListComparison(ctx.inListStringComparison().stringList());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQueryInList("tasks." + taskId + ".userId", stringList, not));
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
                // todo: sorting by data value, options
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

    @Override
    public void exitIdNull(QueryLangParser.IdNullContext ctx) {
        Predicate mongoQuery;
        Token op = ctx.nullComparison().op;
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        checkOp(ComparisonType.NULL, op);

        switch (resourceType) {
            case PROCESS:
                mongoQuery = isNotNull ? QPetriNet.petriNet._id.isNotNull() : QPetriNet.petriNet._id.isNull();
                break;
            case CASE:
                mongoQuery = isNotNull ? QCase.case$._id.isNotNull() : QCase.case$._id.isNull();
                setElasticQuery(ctx, isNotNull ? "_exists_:stringId" : "!(_exists_:stringId)");
                break;
            case TASK:
                mongoQuery = isNotNull ? QTask.task._id.isNotNull() : QTask.task._id.isNull();
                setElasticQuery(ctx, isNotNull ? "_exists_:stringId" : "!(_exists_:stringId)");
                break;
            case USER:
                mongoQuery = isNotNull ? QUser.user._id.isNotNull() : QUser.user._id.isNull();
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, mongoQuery);
    }

    @Override
    public void exitTitleNull(QueryLangParser.TitleNullContext ctx) {
        Predicate mongoQuery;
        Token op = ctx.nullComparison().op;
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        checkOp(ComparisonType.NULL, op);

        switch (resourceType) {
            case PROCESS:
                mongoQuery = isNotNull ? QPetriNet.petriNet.title.isNotNull() : QPetriNet.petriNet.title.isNull();
                break;
            case CASE:
                mongoQuery = isNotNull ? QCase.case$.title.isNotNull() : QCase.case$.title.isNull();
                setElasticQuery(ctx, isNotNull ? "_exists_:title" : "!(_exists_:title)");
                break;
            case TASK:
                mongoQuery = isNotNull ? QTask.task.title.isNotNull() : QTask.task.title.isNull();
                setElasticQuery(ctx, isNotNull ? "_exists_:title" : "!(_exists_:title)");
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, mongoQuery);
    }

    @Override
    public void exitIdentifierNull(QueryLangParser.IdentifierNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QPetriNet.petriNet.identifier.isNotNull() : QPetriNet.petriNet.identifier.isNull());
    }

    @Override
    public void exitVersionNull(QueryLangParser.VersionNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QPetriNet.petriNet.version.isNotNull() : QPetriNet.petriNet.version.isNull());
    }

    @Override
    public void exitCdNull(QueryLangParser.CdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());

        Predicate mongoQuery;
        switch (resourceType) {
            case PROCESS:
                mongoQuery = isNotNull ? QPetriNet.petriNet.creationDate.isNotNull() : QPetriNet.petriNet.creationDate.isNull();
                break;
            case CASE:
                mongoQuery = isNotNull ? QCase.case$.creationDate.isNotNull() : QCase.case$.creationDate.isNull();
                setElasticQuery(ctx, isNotNull ? "_exists_:creationDateSortable" : "!(_exists_:creationDateSortable)");
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        setMongoQuery(ctx, mongoQuery);
    }

    @Override
    public void exitProcessIdNull(QueryLangParser.ProcessIdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QTask.task.processId.isNotNull() : QTask.task.processId.isNull() );
        setElasticQuery(ctx, isNotNull ? "_exists_:processId" : "!(_exists_:processId)");
    }

    @Override
    public void exitProcessIdObjNull(QueryLangParser.ProcessIdObjNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QCase.case$.petriNetObjectId.isNotNull() : QCase.case$.petriNetObjectId.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:processId" : "!(_exists_:processId)");
    }

    @Override
    public void exitProcessIdentifierNull(QueryLangParser.ProcessIdentifierNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QCase.case$.processIdentifier.isNotNull() : QCase.case$.processIdentifier.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:processIdentifier" : "!(_exists_:processIdentifier)");
    }

    @Override
    public void exitAuthorNull(QueryLangParser.AuthorNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QCase.case$.author.id.isNotNull() : QCase.case$.author.id.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:author" : "!(_exists_:author)");
    }

    @Override
    public void exitTransitionIdNull(QueryLangParser.TransitionIdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QTask.task.transitionId.isNotNull() : QTask.task.transitionId.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:transitionId" : "!(_exists_:transitionId)");
    }

    @Override
    public void exitUserIdNull(QueryLangParser.UserIdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QTask.task.userId.isNotNull() : QTask.task.userId.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:userId" : "!(_exists_:userId)");
    }

    @Override
    public void exitLfNull(QueryLangParser.LfNullContext ctx) {
        // todo implement lastFinished
    }

    @Override
    public void exitCaseIdNull(QueryLangParser.CaseIdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QTask.task.caseId.isNotNull() : QTask.task.caseId.isNull());
        setElasticQuery(ctx, isNotNull ? "_exists_:caseId" : "!(_exists_:caseId)");
    }

    @Override
    public void exitLaNull(QueryLangParser.LaNullContext ctx) {
        // todo implement lastAssigned
    }

    @Override
    public void exitNameNull(QueryLangParser.NameNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QUser.user.name.isNotNull() : QUser.user.name.isNull());
    }

    @Override
    public void exitSurnameNull(QueryLangParser.SurnameNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QUser.user.surname.isNotNull() : QUser.user.surname.isNull());
    }

    @Override
    public void exitEmailNull(QueryLangParser.EmailNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        setMongoQuery(ctx, isNotNull ? QUser.user.email.isNotNull() : QUser.user.email.isNull());
    }

    @Override
    public void exitDataNull(QueryLangParser.DataNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        String fieldId = ctx.dataValue().fieldId.getText();
        String elasticAttribute = "dataSet." + fieldId + ".fulltextValue";

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, isNotNull ? "_exists_:" + elasticAttribute : "!(_exists_:" + elasticAttribute + ")");
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsNull(QueryLangParser.DataOptionsNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        String fieldId = ctx.dataOptions().fieldId.getText();
        String elasticAttribute = "dataSet." + fieldId + ".options";

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, isNotNull ? "_exists_:" + elasticAttribute : "!(_exists_:" + elasticAttribute + ")");
        this.searchWithElastic = true;
    }

    @Override
    public void exitPlacesNull(QueryLangParser.PlacesNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        String placeId = ctx.places().placeId.getText();
        String elasticAttribute = "places." + placeId + ".marking";

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, isNotNull ? "_exists_:" + elasticAttribute : "!(_exists_:" + elasticAttribute + ")");
        this.searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdNull(QueryLangParser.TasksUserIdNullContext ctx) {
        Token op = ctx.nullComparison().op;
        checkOp(ComparisonType.NULL, op);
        boolean isNotNull = shouldBeNotNull(ctx.nullComparison());
        String taskId = ctx.tasksUserId().taskId.getText();
        String elasticAttribute = "tasks." + taskId + ".userId";

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, isNotNull ? "_exists_:" + elasticAttribute : "!(_exists_:" + elasticAttribute + ")");
        this.searchWithElastic = true;
    }

    @Override
    public void exitTitleLike(QueryLangParser.TitleLikeContext ctx) {
        StringPath stringPath;
        Token op = ctx.stringLikeComparison().stringComparison().op;
        checkOp(ComparisonType.LIKE, op);
        boolean not = ctx.stringLikeComparison().stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringLikeComparison().stringComparison());

        switch (resourceType) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                setElasticQuery(ctx, buildElasticQuery("title", op.getType(), string + "~" + elasticFuzzyMaxDistance, not));
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                setElasticQuery(ctx, buildElasticQuery("title", op.getType(), string + "~" + elasticFuzzyMaxDistance, not));
                break;
            default:
                throw new IllegalArgumentException("Unknown query type: " + resourceType);
        }

        boolean negate = (op.getType() == QueryLangParser.NEQ) != not;
        Predicate mongoQuery = stringPath.likeIgnoreCase("%" + string + "%");
        setMongoQuery(ctx, negate ? mongoQuery.not() : mongoQuery);
    }

    @Override
    public void exitDataStringLike(QueryLangParser.DataStringLikeContext ctx) {
        String fieldId = ctx.dataValue().fieldId.getText();
        Token op = ctx.stringLikeComparison().stringComparison().op;
        checkOp(ComparisonType.LIKE, op);
        boolean not = ctx.stringLikeComparison().stringComparison().NOT() != null;
        String string = handleStringComparisonWithPlaceholders(ctx.stringLikeComparison().stringComparison());

        setMongoQuery(ctx, null);
        setElasticQuery(ctx, buildElasticQuery("dataSet." + fieldId + ".fulltextValue", op.getType(),
                string + "~" + elasticFuzzyMaxDistance, not));
        this.searchWithElastic = true;
    }

    private boolean shouldBeNotNull(QueryLangParser.NullComparisonContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Null comparison context must be provided");
        }
        if (ctx.EQ() == null && ctx.NEQ() == null) {
            throw new IllegalArgumentException("Any of the operators EQ or NEQ must be used");
        }
        return ctx.NOT() != null && ctx.EQ() != null || ctx.NOT() == null && ctx.NEQ() != null;
    }
}
