package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.antlr.v4.runtime.Token;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SearchUtils {

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
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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

    private static Predicate buildStringPredicate(StringPath stringPath, Token op, String string) {
        switch (op.getType()) {
            case QueryLangParser.EQ:
                return stringPath.eq(string);
            case QueryLangParser.CONTAINS:
                return stringPath.contains(string);
        }

        throw new UnsupportedOperationException("Operator " + op.getText() + " is not available for string comparison");
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

    public static String getElasticQuery(String attribute, Token op, String value) {
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
