package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangBaseVisitor;
import com.netgrif.application.engine.antlr4.QueryLangLexer;
import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.QVersion;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.State;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

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
        if (ctx.processAndExpression().size() > 1) {
//            return visit(ctx.processAndExpression(0)) | visit(ctx.processAndExpression(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.processAndExpression(0));
    }

    @Override
    public Predicate visitProcessAndExpression(QueryLangParser.ProcessAndExpressionContext ctx) {
        if (ctx.processConditionGroup().size() > 1) {
//            return visit(ctx.processConditionGroup(0)) & visit(ctx.processConditionGroup(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.processConditionGroup(0));
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
        if (ctx.caseAndExpression().size() > 1) {
//            return visit(ctx.caseAndExpression(0)) | visit(ctx.caseAndExpression(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.caseAndExpression(0));
    }

    @Override
    public Predicate visitCaseAndExpression(QueryLangParser.CaseAndExpressionContext ctx) {
        if (ctx.caseConditionGroup().size() > 1) {
//            return visit(ctx.caseConditionGroup(0)) & visit(ctx.caseConditionGroup(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.caseConditionGroup(0));
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
        if (ctx.taskAndExpression().size() > 1) {
//            return visit(ctx.taskAndExpression(0)) | visit(ctx.taskAndExpression(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.taskAndExpression(0));
    }

    @Override
    public Predicate visitTaskAndExpression(QueryLangParser.TaskAndExpressionContext ctx) {
        if (ctx.taskConditionGroup().size() > 1) {
//            return visit(ctx.taskConditionGroup(0)) & visit(ctx.taskConditionGroup(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.taskConditionGroup(0));
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
        if (ctx.userAndExpression().size() > 1) {
//            return visit(ctx.userAndExpression(0)) | visit(ctx.userAndExpression(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.userAndExpression(0));
    }

    @Override
    public Predicate visitUserAndExpression(QueryLangParser.UserAndExpressionContext ctx) {
        if (ctx.userConditionGroup().size() > 1) {
//            return visit(ctx.userConditionGroup(0)) & visit(ctx.userConditionGroup(1)); // todo NAE-1997: Operator '|' cannot be applied to 'com. querydsl. core. types. Predicate', 'com. querydsl. core. types. Predicate'
        }
        return visit(ctx.userConditionGroup(0));
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
                throw new UnsupportedOperationException();
        }
        String string = ctx.stringComparison().STRING().getText();

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
                throw new UnsupportedOperationException();
        }
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitIdentifierComparison(QueryLangParser.IdentifierComparisonContext ctx) {
        StringPath stringPath = QPetriNet.petriNet.identifier;
        String string = ctx.stringComparison().STRING().getText();

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
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate visitCreationDateComparison(QueryLangParser.CreationDateComparisonContext ctx) {
        // todo NAE-1997: implement date/datetime comparison
        return super.visitCreationDateComparison(ctx);
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
                throw new UnsupportedOperationException();
        }
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitAuthorComparison(QueryLangParser.AuthorComparisonContext ctx) {
        StringPath stringPath = QCase.case$.author.id;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitTransitionIdComparison(QueryLangParser.TransitionIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.transitionId;
        String string = ctx.stringComparison().STRING().getText();

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

        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate visitUserIdComparison(QueryLangParser.UserIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.userId;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitCaseIdComparison(QueryLangParser.CaseIdComparisonContext ctx) {
        StringPath stringPath = QTask.task.caseId;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitLastAssignComparison(QueryLangParser.LastAssignComparisonContext ctx) {
        // todo NAE-1997: implement date/datetime comparison
        return super.visitLastAssignComparison(ctx);
    }

    @Override
    public Predicate visitLastFinishComparison(QueryLangParser.LastFinishComparisonContext ctx) {
        // todo NAE-1997: implement date/datetime comparison
        return super.visitLastFinishComparison(ctx);
    }

    @Override
    public Predicate visitNameComparison(QueryLangParser.NameComparisonContext ctx) {
        StringPath stringPath = QUser.user.name;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitSurnameComparison(QueryLangParser.SurnameComparisonContext ctx) {
        StringPath stringPath = QUser.user.surname;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitEmailComparison(QueryLangParser.EmailComparisonContext ctx) {
        StringPath stringPath = QUser.user.email;
        String string = ctx.stringComparison().STRING().getText();

        return evaluateStringComparison(stringPath, ctx.stringComparison());
    }

    @Override
    public Predicate visitDataComparison(QueryLangParser.DataComparisonContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate visitPlacesComparison(QueryLangParser.PlacesComparisonContext ctx) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Predicate visitTasksComparison(QueryLangParser.TasksComparisonContext ctx) {
        throw new UnsupportedOperationException();
    }

    private static Predicate evaluateStringComparison(StringPath stringPath, QueryLangParser.StringComparisonContext ctx) {
        String string = ctx.STRING().getText();
        switch (ctx.op.getType()) {
            case QueryLangParser.EQ:
                return stringPath.eq(string);
            case QueryLangParser.CONTAINS:
                return stringPath.contains(string);
        }

        throw new UnsupportedOperationException();
    }
}
