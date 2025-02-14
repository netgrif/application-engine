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
import com.querydsl.core.types.Predicate;
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

@Slf4j
public class SearchUtils {

    public static final Map<ComparisonType, List<Integer>> comparisonOperators = Map.of(
            ComparisonType.ID, List.of(QueryLangParser.EQ),
            ComparisonType.STRING, List.of(QueryLangParser.EQ, QueryLangParser.CONTAINS),
            ComparisonType.NUMBER, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATE, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATETIME, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.BOOLEAN, List.of(QueryLangParser.EQ),
            ComparisonType.OPTIONS, List.of(QueryLangParser.EQ, QueryLangParser.CONTAINS)
    );

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

        String treeStringVisualisation = evaluator.getRoot().toString();
        if (!errorListener.getErrorMessages().isEmpty()) {
            throw new IllegalArgumentException("\n" + treeStringVisualisation + "\n" + String.join("\n", errorListener.getErrorMessages()));
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
        return "\n" + evaluator.getRoot().toString();
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

    public static Predicate buildObjectIdPredicate(QObjectId qObjectId, Token op, ObjectId objectId, boolean not) {
        if (op.getType() != QueryLangParser.EQ) {
            throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for id comparison");
        }

        Predicate predicate = qObjectId.eq(objectId);
        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildStringPredicate(StringPath stringPath, Token op, String string, boolean not) {
        Predicate predicate = null;
        switch (op.getType()) {
            case QueryLangParser.EQ:
                predicate = stringPath.eq(string);
                break;
            case QueryLangParser.CONTAINS:
                predicate = stringPath.contains(string);
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for string comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildVersionPredicate(Token op, String versionString, boolean not) {
        String[] versionNumber = versionString.split("\\.");
        long major = Long.parseLong(versionNumber[0]);
        long minor = Long.parseLong(versionNumber[1]);
        long patch = Long.parseLong(versionNumber[2]);

        QVersion qVersion = QPetriNet.petriNet.version;

        Predicate predicate = null;
        switch (op.getType()) {
            case QueryLangParser.EQ:
                predicate = qVersion.eq(new Version(major, minor, patch));
                break;
            case QueryLangParser.GT:
                predicate = qVersion.major.gt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.gt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.gt(patch))));
                break;
            case QueryLangParser.GTE:
                predicate = qVersion.major.gt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.gt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.gt(patch))))
                        .or(qVersion.eq(new Version(major, minor, patch)));
                break;
            case QueryLangParser.LT:
                predicate = qVersion.major.lt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.lt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.lt(patch))));
                break;
            case QueryLangParser.LTE:
                predicate = qVersion.major.lt(major)
                        .or(qVersion.major.eq(major).and(qVersion.minor.lt(minor)))
                        .or(qVersion.major.eq(major).and(qVersion.minor.eq(minor).and(qVersion.patch.lt(patch))))
                        .or(qVersion.eq(new Version(major, minor, patch)));
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for version comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static Predicate buildDateTimePredicate(DateTimePath<LocalDateTime> dateTimePath, Token op, LocalDateTime localDateTime, boolean not) {
        Predicate predicate = null;
        switch (op.getType()) {
            case QueryLangParser.EQ:
                predicate = dateTimePath.eq(localDateTime);
                break;
            case QueryLangParser.LT:
                predicate = dateTimePath.lt(localDateTime);
                break;
            case QueryLangParser.LTE:
                predicate = dateTimePath.lt(localDateTime).or(dateTimePath.eq(localDateTime));
                break;
            case QueryLangParser.GT:
                predicate = dateTimePath.gt(localDateTime);
                break;
            case QueryLangParser.GTE:
                predicate = dateTimePath.gt(localDateTime).or(dateTimePath.eq(localDateTime));
                break;
        }

        if (predicate == null) {
            throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for date/datetime comparison");
        }

        if (not) {
            predicate = predicate.not();
        }
        return predicate;
    }

    public static String buildElasticQuery(String attribute, Token op, String value, boolean not) {
        String query = null;
        switch (op.getType()) {
            case QueryLangParser.EQ:
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
            throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for elastic comparison");
        }

        if (not) {
            query = "NOT " + query;
        }
        return query;
    }
}
