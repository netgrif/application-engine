package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangBaseVisitor;
import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.QVersion;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.State;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
public class QueryLangMongoEvaluator extends QueryLangBaseVisitor<Predicate> {

    private QueryType type;

    @Override
    public Predicate visitProcessQuery(QueryLangParser.ProcessQueryContext ctx) {
        type = QueryType.fromString("process");
        return visit(ctx.processConditions());
    }

    @Override
    public Predicate visitCaseQuery(QueryLangParser.CaseQueryContext ctx) {
        type = QueryType.fromString("case");
        return visit(ctx.caseConditions());
    }

    @Override
    public Predicate visitTaskQuery(QueryLangParser.TaskQueryContext ctx) {
        type = QueryType.fromString("task");
        return visit(ctx.taskConditions());
    }

    @Override
    public Predicate visitUserQuery(QueryLangParser.UserQueryContext ctx) {
        type = QueryType.fromString("user");
        return visit(ctx.userConditions());
    }

    @Override
    public Predicate visitProcessOrExpression(QueryLangParser.ProcessOrExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.processAndExpression().forEach(processAndExpressionContext -> builder.or(visit(processAndExpressionContext)));
        return builder;
    }

    @Override
    public Predicate visitProcessAndExpression(QueryLangParser.ProcessAndExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.processConditionGroup().forEach(processConditionGroupContext -> builder.and(visit(processConditionGroupContext)));
        return builder;
    }

    @Override
    public Predicate visitProcessConditionGroup(QueryLangParser.ProcessConditionGroupContext ctx) {
        if (ctx.processCondition() != null) {
            return visit(ctx.processCondition());
        }
        return ( visit(ctx.processConditions()) );
    }

    @Override
    public Predicate visitProcessCondition(QueryLangParser.ProcessConditionContext ctx) {
        if (ctx.NOT() != null) {
            return visit(ctx.processComparisons()).not();
        }
        return visit(ctx.processComparisons());
    }

    @Override
    public Predicate visitCaseOrExpression(QueryLangParser.CaseOrExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.caseAndExpression().forEach(caseAndExpressionContext -> builder.or(visit(caseAndExpressionContext)));
        return builder;
    }

    @Override
    public Predicate visitCaseAndExpression(QueryLangParser.CaseAndExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.caseConditionGroup().forEach(caseConditionGroupContext -> builder.and(visit(caseConditionGroupContext)));
        return builder;
    }

    @Override
    public Predicate visitCaseConditionGroup(QueryLangParser.CaseConditionGroupContext ctx) {
        if (ctx.caseCondition() != null) {
            return visit(ctx.caseCondition());
        }
        return ( visit(ctx.caseConditions()) );
    }

    @Override
    public Predicate visitCaseCondition(QueryLangParser.CaseConditionContext ctx) {
        if (ctx.NOT() != null) {
            return visit(ctx.caseComparisons()).not();
        }
        return visit(ctx.caseComparisons());
    }

    @Override
    public Predicate visitTaskOrExpression(QueryLangParser.TaskOrExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.taskAndExpression().forEach(taskAndExpressionContext -> builder.or(visit(taskAndExpressionContext)));
        return builder;
    }

    @Override
    public Predicate visitTaskAndExpression(QueryLangParser.TaskAndExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.taskConditionGroup().forEach(taskConditionGroupContext -> builder.and(visit(taskConditionGroupContext)));
        return builder;
    }

    @Override
    public Predicate visitTaskConditionGroup(QueryLangParser.TaskConditionGroupContext ctx) {
        if (ctx.taskCondition() != null) {
            return visit(ctx.taskCondition());
        }
        return ( visit(ctx.taskConditions()) );
    }

    @Override
    public Predicate visitTaskCondition(QueryLangParser.TaskConditionContext ctx) {
        if (ctx.NOT() != null) {
            return visit(ctx.taskComparisons()).not();
        }
        return visit(ctx.taskComparisons());
    }

    @Override
    public Predicate visitUserOrExpression(QueryLangParser.UserOrExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.userAndExpression().forEach(userAndExpressionContext -> builder.or(visit(userAndExpressionContext)));
        return builder;
    }

    @Override
    public Predicate visitUserAndExpression(QueryLangParser.UserAndExpressionContext ctx) {
        BooleanBuilder builder = new BooleanBuilder();
        ctx.userConditionGroup().forEach(userConditionGroupContext -> builder.and(visit(userConditionGroupContext)));
        return builder;
    }

    @Override
    public Predicate visitUserConditionGroup(QueryLangParser.UserConditionGroupContext ctx) {
        if (ctx.userCondition() != null) {
            return visit(ctx.userCondition());
        }
        return ( visit(ctx.userConditions()) );
    }

    @Override
    public Predicate visitUserCondition(QueryLangParser.UserConditionContext ctx) {
        if (ctx.NOT() != null) {
            return visit(ctx.userComparisons()).not();
        }
        return visit(ctx.userComparisons());
    }

    @Override
    public Predicate visitIdComparison(QueryLangParser.IdComparisonContext ctx) {
        StringPath stringPath;
        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.stringId;
                break;
            case CASE:
                stringPath = QCase.case$.stringId;
                break;
            case TASK:
                stringPath = QTask.task.stringId;
                break;
            case USER:
                stringPath = QUser.user.stringId;
                break;
            default:
                throw new IllegalArgumentException("Search by id is not available for type " + type.name());
        }

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitTitleComparison(QueryLangParser.TitleComparisonContext ctx) {
        StringPath stringPath;
        switch (type) {
            case PROCESS:
                stringPath = QPetriNet.petriNet.title.defaultValue;
                break;
            case CASE:
                stringPath = QCase.case$.title;
                break;
            case TASK:
                stringPath = QTask.task.title.defaultValue;
                break;
            default:
                throw new IllegalArgumentException("Search by title is not available for type " + type.name());
        }

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitIdentifierComparison(QueryLangParser.IdentifierComparisonContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitVersionComparison(QueryLangParser.VersionComparisonContext ctx) {
        String[] versionNumber = ctx.VERSION_NUMBER().getText().split("\\.");
        long major = Long.parseLong(versionNumber[0]);
        long minor = Long.parseLong(versionNumber[1]);
        long patch = Long.parseLong(versionNumber[2]);

        QVersion qVersion = QPetriNet.petriNet.version;

        switch (ctx.op.getType()) {
            case QueryLangParser.EQ:
                return qVersion.eq(new Version(major, minor, patch));
            case QueryLangParser.GT:
                return qVersion.major.gt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.gt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.gt(patch))));
            case QueryLangParser.GTE:
                return qVersion.major.gt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.gt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.gt(patch))))
                        .or(qVersion.eq(new Version(major, minor, patch)));
            case QueryLangParser.LT:
                return qVersion.major.lt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.lt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.lt(patch))));
            case QueryLangParser.LTE:
                return qVersion.major.lt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.lt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.lt(patch))))
                        .or(qVersion.eq(new Version(major, minor, patch)));
        }
        throw new UnsupportedOperationException("Operator " + ctx.op.getText() + " is not available for version comparison");
    }

    @Override
    public Predicate visitCreationDateComparison(QueryLangParser.CreationDateComparisonContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath;
        switch (type) {
            case PROCESS:
                dateTimePath = QPetriNet.petriNet.creationDate;
                break;
            case CASE:
                dateTimePath = QCase.case$.creationDate;
                break;
            default:
                throw new IllegalArgumentException("Search by creation date is not available for type " + type.name());
        }

        return evaluateDateOrDateTimeComparison(dateTimePath, ctx.dateComparison(), ctx.dateTimeComparison());
    }

    @Override
    public Predicate visitProcessIdComparison(QueryLangParser.ProcessIdComparisonContext ctx) {
        StringPath stringPath;
        switch (type) {
            case CASE:
                stringPath = QCase.case$.petriNetId;
                break;
            case TASK:
                stringPath = QTask.task.processId;
                break;
            default:
                throw new IllegalArgumentException("Search by process id is not available for type " + type.name());
        }

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitAuthorComparison(QueryLangParser.AuthorComparisonContext ctx) {
        StringPath stringPath = QCase.case$.author.id;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitTransitionIdComparison(QueryLangParser.TransitionIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.transitionId;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitStateComparison(QueryLangParser.StateComparisonContext ctx) {
        switch (ctx.state.getType()) {
            case QueryLangParser.ENABLED:
                return QTask.task.state.eq(State.ENABLED);
            case QueryLangParser.DISABLED:
                return QTask.task.state.eq(State.DISABLED);
        }

        throw new IllegalArgumentException("Invalid task state: " + ctx.state.getType());
    }

    @Override
    public Predicate visitUserIdComparison(QueryLangParser.UserIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.userId;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitCaseIdComparison(QueryLangParser.CaseIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.caseId;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitLastAssignComparison(QueryLangParser.LastAssignComparisonContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastAssigned;

        return evaluateDateOrDateTimeComparison(dateTimePath, ctx.dateComparison(), ctx.dateTimeComparison());
    }

    @Override
    public Predicate visitLastFinishComparison(QueryLangParser.LastFinishComparisonContext ctx) {
        DateTimePath<LocalDateTime> dateTimePath = QTask.task.lastFinished;

        return evaluateDateOrDateTimeComparison(dateTimePath, ctx.dateComparison(), ctx.dateTimeComparison());
    }

    @Override
    public Predicate visitNameComparison(QueryLangParser.NameComparisonContext ctx) {
        StringPath stringPath = QUser.user.name;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitSurnameComparison(QueryLangParser.SurnameComparisonContext ctx) {
        StringPath stringPath = QUser.user.surname;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitEmailComparison(QueryLangParser.EmailComparisonContext ctx) {
        StringPath stringPath = QUser.user.email;

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitDataComparison(QueryLangParser.DataComparisonContext ctx) {
        throw new UnsupportedOperationException("Case search by data is not available for MongoDB");
    }

    @Override
    public Predicate visitPlacesComparison(QueryLangParser.PlacesComparisonContext ctx) {
        throw new UnsupportedOperationException("Case search by place is not available for MongoDB");

    }

    @Override
    public Predicate visitTasksComparison(QueryLangParser.TasksComparisonContext ctx) {
        throw new UnsupportedOperationException("Case search by tasks is not available for MongoDB");
    }

    private static Predicate evaluateStringComparison(StringPath stringPath, QueryLangParser.StringComparisonContext ctx) {
        String string = ctx.STRING().getText();
        switch (ctx.op.getType()) {
            case QueryLangParser.EQ:
                return stringPath.eq(string);
            case QueryLangParser.CONTAINS:
                return stringPath.contains(string);
        }

        throw new UnsupportedOperationException("Operator " + ctx.op.getText() + " is not available for string comparison");
    }

    private static Predicate evaluateDateOrDateTimeComparison(DateTimePath<LocalDateTime> dateTimePath, QueryLangParser.DateComparisonContext dateComparisonContext, QueryLangParser.DateTimeComparisonContext dateTimeComparisonContext) {
        if (dateComparisonContext != null) {
            return getDateTimePredicate(dateTimePath, dateComparisonContext.op, dateComparisonContext.DATE().getText());
        } else if (dateTimeComparisonContext != null) {
            return getDateTimePredicate(dateTimePath, dateTimeComparisonContext.op, dateTimeComparisonContext.DATETIME().getText());
        }
        throw new IllegalArgumentException("Date or date time comparison expected");
    }

    private static Predicate getDateTimePredicate(DateTimePath<LocalDateTime> dateTimePath, Token op, String input) {
        LocalDateTime localDateTime = getLocalDateTime(input);

        switch (op.getType()) {
            case QueryLangParser.EQ:
                return dateTimePath.eq(localDateTime);
            case QueryLangParser.LT:
                return dateTimePath.lt(localDateTime);
            case QueryLangParser.LTE:
                return dateTimePath.lt(localDateTime).or(dateTimePath.eq(localDateTime));
            case QueryLangParser.GT:
                return dateTimePath.gt(localDateTime);
            case QueryLangParser.GTE:
                return dateTimePath.gt(localDateTime).or(dateTimePath.eq(localDateTime));
        }

        throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for date/datetime comparison");
    }

    private static LocalDateTime getLocalDateTime(String input) {
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(SearchService.DATE_TIME_PATTERN));
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern(SearchService.DATE_PATTERN)).atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date/datetime format");
            }
        }
    }
}
