package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.netgrif.application.engine.petrinet.domain.QPetriNet;
import com.netgrif.application.engine.petrinet.domain.version.QVersion;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.search.enums.ComparisonType;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.antlr.v4.runtime.Token;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class SearchUtils {

    public static final Map<ComparisonType, List<Integer>> comparisonOperators = Map.of(
            ComparisonType.STRING, List.of(QueryLangParser.EQ, QueryLangParser.CONTAINS),
            ComparisonType.NUMBER, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATE, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.DATETIME, List.of(QueryLangParser.EQ, QueryLangParser.LT, QueryLangParser.LTE, QueryLangParser.GT, QueryLangParser.GTE),
            ComparisonType.BOOLEAN, List.of(QueryLangParser.EQ),
            ComparisonType.OPTIONS, List.of(QueryLangParser.CONTAINS)
    );

    public static String toDateString(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String toDateString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String toDateTimeString(LocalDate localDate) {
        return localDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String toDateTimeString(LocalDateTime localDateTime) {
        return localDateTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static LocalDateTime toDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
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

    public static String getStringValue(String queryLangString) {
        return queryLangString.replace("'", "");
    }

    public static void checkOp(ComparisonType type, Token op) {
        if (!comparisonOperators.get(type).contains(op.getType())) {
            throw new IllegalArgumentException("Operator " + op.getText() + " is not applicable for type " + type.toString());
        }
    }

    public static Predicate buildStringPredicate(StringPath stringPath, Token op, String string) {
        switch (op.getType()) {
            case QueryLangParser.EQ:
                return stringPath.eq(string);
            case QueryLangParser.CONTAINS:
                return stringPath.contains(string);
        }

        throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for string comparison");
    }

    public static Predicate buildVersionPredicate(Token op, String versionString) {
        String[] versionNumber = versionString.split("\\.");
        long major = Long.parseLong(versionNumber[0]);
        long minor = Long.parseLong(versionNumber[1]);
        long patch = Long.parseLong(versionNumber[2]);

        QVersion qVersion = QPetriNet.petriNet.version;

        switch (op.getType()) {
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
        throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for version comparison");
    }

    public static Predicate buildDateTimePredicate(DateTimePath<LocalDateTime> dateTimePath, Token op, LocalDateTime localDateTime) {
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

    public static String buildElasticQuery(String attribute, Token op, String value) {
        switch (op.getType()) {
            case QueryLangParser.EQ:
                return attribute + ":" + value;
            case QueryLangParser.LT:
                return attribute + ":<" + value;
            case QueryLangParser.LTE:
                return attribute + ":<=" + value;
            case QueryLangParser.GT:
                return attribute + ":>" + value;
            case QueryLangParser.GTE:
                return attribute + ":>=" + value;
            case QueryLangParser.CONTAINS:
                return attribute + ":*" + value + "*";
        }
        throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for elastic comparison");
    }
}
