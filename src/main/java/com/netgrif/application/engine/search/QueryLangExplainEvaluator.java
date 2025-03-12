package com.netgrif.application.engine.search;

import com.netgrif.application.engine.search.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.search.antlr4.QueryLangParser;
import com.netgrif.application.engine.search.enums.QueryType;
import com.netgrif.application.engine.search.utils.QueryLangTreeNode;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.netgrif.application.engine.search.utils.SearchUtils.*;

public class QueryLangExplainEvaluator extends QueryLangBaseListener {

    private final ParseTreeProperty<QueryLangTreeNode> node = new ParseTreeProperty<>();
    @Getter
    private QueryLangTreeNode root = null;

    @Getter
    private QueryType type;
    @Getter
    private boolean multiple;
    @Getter
    private boolean searchWithElastic = false;

    private int pageNumber = 0;
    private int pageSize = 20;
    private final List<String> sortOrders = new ArrayList<>();

    public String explain() {
        StringBuilder result = new StringBuilder("Searching ");

        result.append(multiple ? "multiple instances" : "single instance")
                .append(" of resource ")
                .append(type != null ? type.name() : "invalid type")
                .append(" with ")
                .append(searchWithElastic ? "Elasticsearch" : "MongoDB")
                .append(".\n")
                .append("page number: ").append(pageNumber)
                .append(", page size: ").append(pageSize)
                .append("\n");

        if (!sortOrders.isEmpty()) {
            result.append("sort by ").append(String.join(";", sortOrders)).append("\n");
        }

        result.append(root != null ? root.toString() : "Tree visualisation not available.");

        return result.toString();
    }

    public void setQueryLangTreeNode(ParseTree node, QueryLangTreeNode queryLangTreeNode) {
        if (queryLangTreeNode == null) {
            queryLangTreeNode = new QueryLangTreeNode("error: " + node.getText());
        }
        this.node.put(node, queryLangTreeNode);
    }

    public QueryLangTreeNode getQueryLangTreeNode(ParseTree node) {
        return this.node.get(node);
    }

    private static QueryLangTreeNode createTreeNode(String name, List<QueryLangTreeNode> children, List<QueryLangTreeNode> errors) {
        List<QueryLangTreeNode> combinedChildren = Stream.concat(children.stream(), errors.stream()).collect(Collectors.toList());
        return new QueryLangTreeNode(name, combinedChildren);
    }

    private QueryLangTreeNode getErrorFromNode(ParseTree node) {
        if (node instanceof ErrorNodeImpl) {
            String errorMsg = "error: " + ((ErrorNodeImpl) node).symbol.getText();
            return new QueryLangTreeNode(errorMsg);
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
        List<QueryLangTreeNode> errorNodes = getErrorsFromChildren(children);

        List<QueryLangTreeNode> childrenNodes = children.stream()
                .map(child -> {
                    QueryLangTreeNode node = getQueryLangTreeNode(child);
                    if (node == null) {
                        errorNodes.addAll(getErrorsRecursive(child));
                    }
                    return node;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        setQueryLangTreeNode(current, createTreeNode(nodeName, childrenNodes, errorNodes));
    }

    @Override
    public void enterProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        type = QueryType.PROCESS;
        multiple = ctx.resource.getType() == QueryLangParser.PROCESSES;
    }

    @Override
    public void exitProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        root = createTreeNode("process query", List.of(getQueryLangTreeNode(ctx.processConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        type = QueryType.CASE;
        multiple = ctx.resource.getType() == QueryLangParser.CASES;
    }

    @Override
    public void exitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        QueryLangTreeNode childTreeNode = getQueryLangTreeNode(ctx.caseConditions());
        root = createTreeNode("case query", List.of(childTreeNode), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        type = QueryType.TASK;
        multiple = ctx.resource.getType() == QueryLangParser.TASKS;
    }

    @Override
    public void exitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        root = createTreeNode("task query", List.of(getQueryLangTreeNode(ctx.taskConditions())), getErrorsFromChildren(ctx.children));
    }

    @Override
    public void enterUserQuery(QueryLangParser.UserQueryContext ctx) {
        type = QueryType.USER;
        multiple = ctx.resource.getType() == QueryLangParser.USERS;
    }

    @Override
    public void exitUserQuery(QueryLangParser.UserQueryContext ctx) {
        root = createTreeNode("user query", List.of(getQueryLangTreeNode(ctx.userConditions())), getErrorsFromChildren(ctx.children));
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
        setQueryLangTreeNode(ctx, createTreeNode("()", List.of(getQueryLangTreeNode(ctx.processConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitCaseConditions(QueryLangParser.CaseConditionsContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.caseOrExpression()));
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
        setQueryLangTreeNode(ctx, createTreeNode("()", List.of(getQueryLangTreeNode(ctx.caseConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitTaskConditions(QueryLangParser.TaskConditionsContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.taskOrExpression()));
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
        setQueryLangTreeNode(ctx, createTreeNode("()", List.of(getQueryLangTreeNode(ctx.taskConditions())), getErrorsFromChildren(ctx.children)));
    }

    @Override
    public void exitUserConditions(QueryLangParser.UserConditionsContext ctx) {
        setQueryLangTreeNode(ctx, getQueryLangTreeNode(ctx.userOrExpression()));
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
        setQueryLangTreeNode(ctx, createTreeNode("()", List.of(getQueryLangTreeNode(ctx.userConditions())), getErrorsFromChildren(ctx.children)));
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

    @Override
    public void exitPlacesBasic(QueryLangParser.PlacesBasicContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void enterPlacesList(QueryLangParser.PlacesListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void enterPlacesRange(QueryLangParser.PlacesRangeContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitTasksStateComparison(QueryLangParser.TasksStateComparisonContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdBasic(QueryLangParser.TasksUserIdBasicContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitTasksUserIdList(QueryLangParser.TasksUserIdListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataString(QueryLangParser.DataStringContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataNumber(QueryLangParser.DataNumberContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDate(QueryLangParser.DataDateContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDatetime(QueryLangParser.DataDatetimeContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataBoolean(QueryLangParser.DataBooleanContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataDateList(QueryLangParser.DataDateListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataDateRange(QueryLangParser.DataDateRangeContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataNumberList(QueryLangParser.DataNumberListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataNumberRange(QueryLangParser.DataNumberRangeContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataStringList(QueryLangParser.DataStringListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataStringRange(QueryLangParser.DataStringRangeContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsBasic(QueryLangParser.DataOptionsBasicContext ctx) {
        this.searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsList(QueryLangParser.DataOptionsListContext ctx) {
        searchWithElastic = true;
    }

    @Override
    public void exitDataOptionsRange(QueryLangParser.DataOptionsRangeContext ctx) {
        searchWithElastic = true;
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
                sortOrders.add("Invalid attribute: " + attrOrd.caseAttribute().getText());
            }
            sortOrders.add("attribute: " + prop + ", ordering: " + dir);
        });
    }

    @Override
    public void exitProcessSorting(QueryLangParser.ProcessSortingContext ctx) {
        ctx.processAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = processAttrToSortPropMapping.get(attrOrd.processAttribute().getText().toLowerCase());
            if (prop == null) {
                sortOrders.add("Invalid attribute: " + attrOrd.processAttribute().getText());
            }
            sortOrders.add("attribute: " + prop + ", ordering: " + dir);
        });
    }

    @Override
    public void exitTaskSorting(QueryLangParser.TaskSortingContext ctx) {
        ctx.taskAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = taskAttrToSortPropMapping.get(attrOrd.taskAttribute().getText().toLowerCase());
            if (prop == null) {
                sortOrders.add("Invalid attribute: " + attrOrd.taskAttribute().getText());
            }
            sortOrders.add("attribute: " + prop + ", ordering: " + dir);
        });
    }

    @Override
    public void exitUserSorting(QueryLangParser.UserSortingContext ctx) {
        ctx.userAttributeOrdering().forEach(attrOrd -> {
            Sort.Direction dir = attrOrd.ordering != null ? Sort.Direction.fromString(attrOrd.ordering.getText()) : Sort.Direction.ASC;
            String prop = userAttrToSortPropMapping.get(attrOrd.userAttribute().getText().toLowerCase());
            if (prop == null) {
                sortOrders.add("Invalid attribute: " + attrOrd.userAttribute().getText());
            }
            sortOrders.add("attribute: " + prop + ", ordering: " + dir);
        });
    }
}
