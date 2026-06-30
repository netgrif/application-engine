package com.netgrif.application.engine.objects.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilsTest {

    @Test
    void formatsLocalDateAndLocalDateTimeUsingEnginePatterns() {
        assertEquals("29.06.2026", DateUtils.toString(LocalDate.of(2026, 6, 29)));
        assertEquals("2026-06-29 14:35", DateUtils.toString(LocalDateTime.of(2026, 6, 29, 14, 35, 42)));
    }

    @Test
    void convertsLocalDateToDateAtSystemStartOfDay() {
        LocalDate localDate = LocalDate.of(2026, 6, 29);

        Date date = DateUtils.localDateToDate(localDate);

        assertEquals(localDate, DateUtils.dateToLocalDate(date));
    }

    @Test
    void convertsLocalDateTimeToDateAtSystemZone() {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 6, 29, 14, 35, 42);

        Date date = DateUtils.localDateTimeToDate(localDateTime);

        assertEquals(localDateTime, DateUtils.dateToLocalDateTime(date));
    }

    @Test
    void convertsLocalValuesToRequestedZone() {
        ZoneId zone = ZoneId.of("Europe/Bratislava");
        LocalDate localDate = LocalDate.of(2026, 6, 29);
        LocalDateTime localDateTime = LocalDateTime.of(2026, 6, 29, 14, 35);

        ZonedDateTime dateAsZone = DateUtils.localDateToZonedDate(localDate, zone);
        ZonedDateTime dateTimeAsZone = DateUtils.localDateTimeToZonedDateTime(localDateTime, zone);

        assertEquals(zone, dateAsZone.getZone());
        assertEquals(localDate.atStartOfDay(), dateAsZone.toLocalDateTime());
        assertEquals(zone, dateTimeAsZone.getZone());
        assertEquals(localDateTime, dateTimeAsZone.toLocalDateTime());
    }
}
