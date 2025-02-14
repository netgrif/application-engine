package com.netgrif.application.engine.search;

import com.netgrif.application.engine.search.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.search.antlr4.QueryLangParser;
import com.netgrif.application.engine.search.enums.QueryType;
import com.netgrif.application.engine.search.utils.QueryLangTreeNode;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryLangExplainEvaluator extends QueryLangBaseListener {

    private final ParseTreeProperty<QueryLangTreeNode> node = new ParseTreeProperty<>();
    @Getter
    private QueryLangTreeNode root = null;

    @Getter
    private QueryType type;
    @Getter
    private Boolean multiple;

    public void setQueryLangTreeNode(ParseTree node, QueryLangTreeNode queryLangTreeNode) {
        this.node.put(node, queryLangTreeNode);
    }

    public QueryLangTreeNode getQueryLangTreeNode(ParseTree node) {
        return this.node.get(node);
    }

    private QueryLangTreeNode getErrorFromNode(ParseTree node) {
        if (node instanceof ErrorNodeImpl) {
            String errorMsg = "error: " + ((ErrorNodeImpl) node).symbol.getText();
            return new QueryLangTreeNode(errorMsg, new ArrayList<>());
        }
        return null;
    }

    private List<QueryLangTreeNode> getErrorsFromChildren(List<ParseTree> children) {
        List<QueryLangTreeNode> errors = new ArrayList<>();
        children.forEach(child -> {
            if (child instanceof ErrorNodeImpl) {
                errors.add(getErrorFromNode(child));
            }
        });
        return errors;
    }

    private List<QueryLangTreeNode> getErrorsRecursive(ParseTree node) {
        List<QueryLangTreeNode> errors = new ArrayList<>();
        if (node.getChildCount() == 0) {
            if (node instanceof ErrorNodeImpl) {
                errors.add(getErrorFromNode(node));
            }
            return errors;
        }

        int numChildren = node.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            errors.addAll(getErrorsRecursive(node.getChild(i)));
        }
        return errors;
    }

    private void processComplexExpression(String nodeName, List<ParseTree> children, ParseTree current) {
        if (children.size() == 1) {
            setQueryLangTreeNode(current, getQueryLangTreeNode(children.get(0)));
            return;
        }

        List<QueryLangTreeNode> childrenNodes = children.stream()
                .map(this::getQueryLangTreeNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        setQueryLangTreeNode(current, new QueryLangTreeNode(nodeName, childrenNodes, getErrorsFromChildren(children)));
    }

    @Override
    public void enterProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        type = QueryType.PROCESS;
        multiple = ctx.resource.getType() == QueryLangParser.PROCESSES;
    }

    @Override
    public void exitProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        root = new QueryLangTreeNode("process query", List.of(getQueryLangTreeNode(ctx.processConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        type = QueryType.CASE;
        multiple = ctx.resource.getType() == QueryLangParser.CASES;
    }

    @Override
    public void exitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        root = new QueryLangTreeNode("case query", List.of(getQueryLangTreeNode(ctx.caseConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        type = QueryType.TASK;
        multiple = ctx.resource.getType() == QueryLangParser.TASKS;
    }

    @Override
    public void exitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        root = new QueryLangTreeNode("task query", List.of(getQueryLangTreeNode(ctx.taskConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterUserQuery(QueryLangParser.UserQueryContext ctx) {
        type = QueryType.USER;
        multiple = ctx.resource.getType() == QueryLangParser.USERS;
    }

    @Override
    public void exitUserQuery(QueryLangParser.UserQueryContext ctx) {
        root = new QueryLangTreeNode("user query", List.of(getQueryLangTreeNode(ctx.userConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void exitProcessConditions(QueryLangParser.ProcessConditionsContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.processOrExpression()));
    }

    @Override
    public void exitProcessOrExpression(QueryLangParser.ProcessOrExpressionContext ctx) {
        List<ParseTree> children = ctx.processAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processComplexExpression("OR", children, ctx);
    }

    @Override
    public void exitProcessAndExpression(QueryLangParser.ProcessAndExpressionContext ctx) {
        List<ParseTree> children = ctx.processConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processComplexExpression("AND", children, ctx);
    }

    @Override
    public void exitProcessConditionGroupBasic(QueryLangParser.ProcessConditionGroupBasicContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.processCondition()));
    }

    @Override
    public void exitProcessConditionGroupParenthesis(QueryLangParser.ProcessConditionGroupParenthesisContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("()", List.of(getQueryLangTreeNode(ctx.processConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitCaseConditions(QueryLangParser.CaseConditionsContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("", List.of(getQueryLangTreeNode(ctx.caseOrExpression())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitCaseOrExpression(QueryLangParser.CaseOrExpressionContext ctx) {
        List<ParseTree> children = ctx.caseAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processComplexExpression("OR", children, ctx);
    }

    @Override
    public void exitCaseAndExpression(QueryLangParser.CaseAndExpressionContext ctx) {
        List<ParseTree> children = ctx.caseConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processComplexExpression("AND", children, ctx);
    }

    @Override
    public void exitCaseConditionGroupBasic(QueryLangParser.CaseConditionGroupBasicContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.caseCondition()));
    }


    @Override
    public void exitCaseConditionGroupParenthesis(QueryLangParser.CaseConditionGroupParenthesisContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("()", List.of(getQueryLangTreeNode(ctx.caseConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitTaskConditions(QueryLangParser.TaskConditionsContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("", List.of(getQueryLangTreeNode(ctx.taskOrExpression())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitTaskOrExpression(QueryLangParser.TaskOrExpressionContext ctx) {
        List<ParseTree> children = ctx.taskAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processComplexExpression("OR", children, ctx);
    }

    @Override
    public void exitTaskAndExpression(QueryLangParser.TaskAndExpressionContext ctx) {
        List<ParseTree> children = ctx.taskConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processComplexExpression("AND", children, ctx);
    }

    @Override
    public void exitTaskConditionGroupBasic(QueryLangParser.TaskConditionGroupBasicContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.taskCondition()));
    }

    @Override
    public void exitTaskConditionGroupParenthesis(QueryLangParser.TaskConditionGroupParenthesisContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("()", List.of(getQueryLangTreeNode(ctx.taskConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitUserConditions(QueryLangParser.UserConditionsContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("", List.of(getQueryLangTreeNode(ctx.userOrExpression())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitUserOrExpression(QueryLangParser.UserOrExpressionContext ctx) {
        List<ParseTree> children = ctx.userAndExpression().stream()
                .map(andExpression -> (ParseTree) andExpression)
                .collect(Collectors.toList());

        processComplexExpression("OR", children, ctx);
    }

    @Override
    public void exitUserAndExpression(QueryLangParser.UserAndExpressionContext ctx) {
        List<ParseTree> children = ctx.userConditionGroup().stream()
                .map(conditionGroup -> (ParseTree) conditionGroup)
                .collect(Collectors.toList());

        processComplexExpression("AND", children, ctx);
    }

    @Override
    public void exitUserConditionGroupBasic(QueryLangParser.UserConditionGroupBasicContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.userCondition()));
    }

    @Override
    public void exitUserConditionGroupParenthesis(QueryLangParser.UserConditionGroupParenthesisContext ctx) {
        setQueryLangTreeNode(ctx, new QueryLangTreeNode("()", List.of(getQueryLangTreeNode(ctx.userConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitProcessCondition(QueryLangParser.ProcessConditionContext ctx) {
        List<QueryLangTreeNode> errors = getErrorsRecursive(ctx);
        setQueryLangTreeNode(ctx, new QueryLangTreeNode(ctx.getText(), errors));
    }

    @Override
    public void exitCaseCondition(QueryLangParser.CaseConditionContext ctx) {
        List<QueryLangTreeNode> errors = getErrorsRecursive(ctx);
        setQueryLangTreeNode(ctx, new QueryLangTreeNode(ctx.getText(), errors));
    }

    @Override
    public void exitTaskCondition(QueryLangParser.TaskConditionContext ctx) {
        List<QueryLangTreeNode> errors = getErrorsRecursive(ctx);
        setQueryLangTreeNode(ctx, new QueryLangTreeNode(ctx.getText(), errors));
    }

    @Override
    public void exitUserCondition(QueryLangParser.UserConditionContext ctx) {
        List<QueryLangTreeNode> errors = getErrorsRecursive(ctx);
        setQueryLangTreeNode(ctx, new QueryLangTreeNode(ctx.getText(), errors));
    }
}
