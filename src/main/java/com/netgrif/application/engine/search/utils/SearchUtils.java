package com.netgrif.application.engine.search.utils;

import com.netgrif.application.engine.search.QueryLangErrorListener;
import com.netgrif.application.engine.search.QueryLangEvaluator;
import com.netgrif.application.engine.search.QueryLangExplainEvaluator;
import com.netgrif.application.engine.search.antlr4.QueryLangBaseListener;
import com.netgrif.application.engine.search.antlr4.QueryLangLexer;
import com.netgrif.application.engine.search.antlr4.QueryLangParser;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.QVersion;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.search.enums.ComparisonType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.bson.types.ObjectId;
import org.bson.types.QObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SearchUtils {

    public static final Map<ComparisonType, List<Integer>> comparisonOperators = Map.of(
            ComparisonType.ID, List.of(QueryLangParser.EQ),
            ComparisonType.STRING, List.of(QueryLangParser.EQ, QueryLangParser.CONTAINS, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.NUMBER, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATE, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATETIME, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.BOOLEAN, List.of(QueryLangParser.EQ)
    );

    public static final Map<String, String> processAttrToSortPropMapping = Map.of(
            "id", "id",
            "identifier", "identifier",
            "version", "version",
            "title", "title.defaultValue",
            "creationdate", "creationDate"
    );

    public static final Map<String, String> caseAttrToSortPropMapping = Map.of(
            "id", "id",
            "processidentifier", "processIdentifier",
            "processid", "petriNetObjectId",
            "title", "title",
            "creationdate", "creationDate",
            "author", "author.id"
    );

    public static final Map<String, String> caseAttrToSortPropElasticMapping = Map.of(
            "id", "stringId.keyword",
            "processidentifier", "processIdentifier.keyword",
            "processid", "processId.keyword",
            "title", "title.keyword",
            "creationdate", "creationDateSortable",
            "author", "author.keyword"
    );

    public static final Map<String, String> taskAttrToSortPropMapping = Map.of(
            "id", "id",
            "transitionid", "transitionId",
            "title", "title.defaultValue",
            "state", "state",
            "userid", "userId",
            "caseid", "caseId",
            "processid", "processId",
            "lastassign", "lastAssigned",
            "lastfinish", "lastFinished"
    );

    public static final Map<String, String> userAttrToSortPropMapping = Map.of(
            "id", "id",
            "name", "name",
            "surname", "surname",
            "email", "email"
    );

    public static final String LEFT_OPEN_ENDPOINT = "(";
    public static final String RIGHT_OPEN_ENDPOINT = ")";

    public static String toDateString(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String toDateString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String toDateTimeString(LocalDate localDate) {
        return localDate.atTime(12, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String toDateTimeString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static LocalDateTime toDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE).atTime(12, 0, 0);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date/datetime format");
            }
        }
    }

    public static LocalDate toDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date/datetime format");
            }
        }
    }

    public static QueryLangExplainEvaluator explainQueryInternal(ParseTreeWalker walker, QueryLangParser.QueryContext query, QueryLangErrorListener errorListener) {
        QueryLangExplainEvaluator evaluator = new QueryLangExplainEvaluator();
        walker.walk(evaluator, query);

        if (!errorListener.getErrorMessages().isEmpty()) {
            throw new IllegalArgumentException("\n" + evaluator.explain() + "\n" + String.join("\n", errorListener.getErrorMessages()));
        }

        return evaluator;
    }

    private static QueryLangBaseListener evaluateQueryInternal(String input, boolean onlyExplain) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be empty.");
        }

        QueryLangLexer lexer = new QueryLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryLangParser parser = new QueryLangParser(tokens);
        QueryLangErrorListener errorListener = new QueryLangErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        QueryLangParser.QueryContext query = parser.query();
        ParseTreeWalker walker = new ParseTreeWalker();

        if (onlyExplain || !errorListener.getErrorMessages().isEmpty()) {
            return explainQueryInternal(walker, query, errorListener);
        }

        QueryLangEvaluator evaluator = new QueryLangEvaluator();
        walker.walk(evaluator, query);

        return evaluator;
    }

    public static QueryLangEvaluator evaluateQuery(String input) {
        return (QueryLangEvaluator) evaluateQueryInternal(input, false);
    }

    public static String explainQuery(String input) {
        QueryLangExplainEvaluator evaluator = (QueryLangExplainEvaluator) evaluateQueryInternal(input, true);
        return evaluator.explain();
    }

    public static String getStringValue(String queryLangString) {
        return queryLangString.replace("'", "");
    }

    public static ObjectId getObjectIdValue(String queryLangString) {
        String objectId = getStringValue(queryLangString);
        if (ObjectId.isValid(objectId)) {
            return new ObjectId(objectId);
        }

        throw new IllegalArgumentException("Invalid objectId: " + objectId);
    }

    public static void checkOp(ComparisonType type, Token op) {
        if (!comparisonOperators.get(type).contains(op.getType())) {
            throw new IllegalArgumentException("Operator " + op.getText() + " is not applicable for type " + type.toString());
        }
    }

    public static Predicate buildObjectIdPredicate(QObjectId qObjectId, int op, ObjectId objectId, boolean not) {
        if (op != QueryLangParser.EQ) {
            throw new UnsupportedOperationException("Operator is not available for id comparison");
        }

        Predicate predicate = qObjectId.eq(objectId);
        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildStringPredicate(StringPath stringPath, int op, String string, boolean not) {
        Predicate predicate = null;
        switch (op) {
            case QueryLangParser.EQ:
                predicate = stringPath.eq(string);
                break;
            case QueryLangParser.CONTAINS:
                predicate = stringPath.contains(string);
                break;
            case QueryLangParser.LT:
                predicate = stringPath.lt(string);
                break;
            case QueryLangParser.LTE:
                predicate = stringPath.loe(string);
                break;
            case QueryLangParser.GT:
                predicate = stringPath.gt(string);
                break;
            case QueryLangParser.GTE:
                predicate = stringPath.goe(string);
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator is not available for string comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildStringPredicateInList(StringPath stringPath, List<String> values, boolean not) {
        Predicate predicate = stringPath.in(values);

        return not ? predicate.not() : predicate;
    }

    public static Predicate buildStringPredicateInRange(StringPath stringPath, String leftValue, boolean leftEndpointOpen, String rightValue, boolean rightEndpointOpen, boolean not) {
        BooleanExpression leftExpression = leftEndpointOpen ? stringPath.gt(leftValue) : stringPath.goe(leftValue);
        BooleanExpression rightExpression = rightEndpointOpen ? stringPath.lt(rightValue) : stringPath.loe(rightValue);
        Predicate predicate = leftExpression.and(rightExpression);

        return not ? predicate.not() : predicate;
    }

    public static Predicate buildVersionPredicate(int op, String versionString, boolean not) {
        String[] versionNumber = versionString.split("\\.");
        long major = Long.parseLong(versionNumber[0]);
        long minor = Long.parseLong(versionNumber[1]);
        long patch = Long.parseLong(versionNumber[2]);

        QVersion qVersion = QPetriNet.petriNet.version;

        Predicate predicate = null;
        switch (op) {
            case QueryLangParser.EQ:
                predicate = qVersion.eq(new Version(major, minor, patch));
                break;
            case QueryLangParser.GT:
                predicate = qVersion.major.gt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.gt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.gt(patch))));
                break;
            case QueryLangParser.GTE:
                predicate = qVersion.major.goe(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.goe(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.goe(patch))));
                break;
            case QueryLangParser.LT:
                predicate = qVersion.major.lt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.lt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.lt(patch))));
                break;
            case QueryLangParser.LTE:
                predicate = qVersion.major.loe(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.loe(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.loe(patch))));
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator is not available for version comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildVersionPredicateInList(List<String> values, boolean not) {
        List<Version> versions = values.stream().map(stringVersion -> {
            String[] versionNumber = stringVersion.split("\\.");
            long major = Long.parseLong(versionNumber[0]);
            long minor = Long.parseLong(versionNumber[1]);
            long patch = Long.parseLong(versionNumber[2]);

            return new Version(major, minor, patch);
        }).collect(Collectors.toList());

        Predicate predicate = QPetriNet.petriNet.version.in(versions);
        return not ? predicate.not() : predicate;
    }

    public static Predicate buildVersionPredicateInRange(String leftValue, boolean leftEndpointOpen, String rightValue, boolean rightEndpointOpen, boolean not) {
        Predicate leftExpression = buildVersionPredicate(leftEndpointOpen ? QueryLangParser.GT : QueryLangParser.GTE, leftValue, false);
        Predicate rightExpression = buildVersionPredicate(rightEndpointOpen ? QueryLangParser.LT : QueryLangParser.LTE, rightValue, false);

        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(leftExpression);
        predicate.and(rightExpression);

        return not ? predicate.not() : predicate;
    }

    public static Predicate buildDateTimePredicate(DateTimePath<LocalDateTime> dateTimePath, int op, LocalDateTime localDateTime, boolean not) {
        Predicate predicate = null;
        switch (op) {
            case QueryLangParser.EQ:
                predicate = dateTimePath.eq(localDateTime);
                break;
            case QueryLangParser.LT:
                predicate = dateTimePath.lt(localDateTime);
                break;
            case QueryLangParser.LTE:
                predicate = dateTimePath.loe(localDateTime);
                break;
            case QueryLangParser.GT:
                predicate = dateTimePath.gt(localDateTime);
                break;
            case QueryLangParser.GTE:
                predicate = dateTimePath.goe(localDateTime);
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator is not available for date/datetime comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildDateTimePredicateInList(DateTimePath<LocalDateTime> dateTimePath, List<String> values, boolean not) {
        List<LocalDateTime> dateTimes = values.stream().map(SearchUtils::toDateTime).collect(Collectors.toList());
        Predicate predicate = dateTimePath.in(dateTimes);

        return not ? predicate.not() : predicate;
    }

    public static Predicate buildDateTimePredicateInRange(DateTimePath<LocalDateTime> dateTimePath, LocalDateTime leftValue, boolean leftEndpointOpen, LocalDateTime rightValue, boolean rightEndpointOpen, boolean not) {
        BooleanExpression leftExpression = leftEndpointOpen ? dateTimePath.gt(leftValue) : dateTimePath.goe(leftValue);
        BooleanExpression rightExpression = rightEndpointOpen ? dateTimePath.lt(rightValue) : dateTimePath.loe(rightValue);
        Predicate predicate = leftExpression.and(rightExpression);

        return not ? predicate.not() : predicate;
    }

    public static String buildElasticQuery(String attribute, int op, String value, boolean not) {
        String query = null;
        switch (op) {
            case QueryLangParser.EQ:
            case QueryLangParser.IN:
                query = attribute + ":" + value;
                break;
            case QueryLangParser.LT:
                query = attribute + ":<" + value;
                break;
            case QueryLangParser.LTE:
                query = attribute + ":<=" + value;
                break;
            case QueryLangParser.GT:
                query = attribute + ":>" + value;
                break;
            case QueryLangParser.GTE:
                query = attribute + ":>=" + value;
                break;
            case QueryLangParser.CONTAINS:
                query = attribute + ":*" + value + "*";
                break;
        }

        if (query == null) {
            throw new UnsupportedOperationException("Operator is not available for elastic comparison");
        }

        if (not) {
            query = "NOT " + query;
        }
        return query;
    }

    public static String buildElasticQueryInList(String attribute, List<String> values, boolean not) {
        String valuesQuery = "(" + String.join(" OR ", values) + ")";
        return buildElasticQuery(attribute, QueryLangParser.IN, valuesQuery, not);
    }

    public static String buildElasticQueryInRange(String attribute, String leftValue, boolean leftEndpointOpen, String rightValue, boolean rightEndpointOpen, boolean not) {
        String query = "("
                + buildElasticQuery(attribute, leftEndpointOpen ? QueryLangParser.GT : QueryLangParser.GTE, leftValue, false)
                + " AND "
                + buildElasticQuery(attribute, rightEndpointOpen ? QueryLangParser.LT : QueryLangParser.LTE, rightValue, false)
                + ")";
        return not ? "NOT " + query : query;
    }
}
