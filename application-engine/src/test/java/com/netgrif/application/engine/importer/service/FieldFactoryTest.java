package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class FieldFactoryTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private FieldFactory fieldFactory;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void contextStartsAndFieldFactoryBeanIsAvailable() {
        assertNotNull(testHelper);
        assertNotNull(fieldFactory);
    }

    @Test
    void parseDateFromStringSupportsExistingLocalDateFormats() {
        LocalDate expected = LocalDate.of(2026, Month.MAY, 29);

        assertEquals(expected, FieldFactory.parseDateFromString("20260529"));
        assertEquals(expected, FieldFactory.parseDateFromString("2026-05-29"));
        assertEquals(expected, FieldFactory.parseDateFromString("29.05.2026"));
    }

    @Test
    void parseDateFromStringUsesLocalZoneForInstantValues() {
        LocalDate expected = LocalDate.of(2026, Month.MAY, 29);
        String serializedDate = Date.from(expected
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant())
                .toInstant()
                .toString();

        assertEquals(
                expected,
                FieldFactory.parseDateFromString(serializedDate)
        );
    }

    @Test
    void parseDateFromStringUsesUtcForEpochMillisValues() {
        long epochMillis = LocalDateTime.of(2026, Month.MAY, 29, 0, 30)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();

        assertEquals(
                LocalDate.of(2026, Month.MAY, 29),
                FieldFactory.parseDateFromString(String.valueOf(epochMillis))
        );
    }

    @Test
    void parseDateFromStringUsesLocalZoneForOffsetValues() {
        String value = "2026-05-29T00:30:00+02:00";
        LocalDate expected = OffsetDateTime.parse(value)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate();

        assertEquals(
                expected,
                FieldFactory.parseDateFromString(value)
        );
    }

    @Test
    void parseDateFromStringReturnsNullForBlankOrInvalidInput() {
        assertNull(FieldFactory.parseDateFromString(null));
        assertNull(FieldFactory.parseDateFromString(" "));
        assertNull(FieldFactory.parseDateFromString("totok"));
        assertNull(FieldFactory.parseDateFromString("not-a-date"));
    }

    @Test
    void parseDateHandlesDateStringLocalDateAndJavaDateValues() {
        Date javaDate = Date.from(LocalDate.of(2026, Month.MAY, 29)
                .atTime(LocalTime.NOON)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        assertEquals(LocalDate.of(2026, Month.MAY, 29), FieldFactory.parseDate("2026-05-29"));
        assertEquals(LocalDate.of(2026, Month.MAY, 29), FieldFactory.parseDate(LocalDate.of(2026, Month.MAY, 29)));
        assertEquals(LocalDate.of(2026, Month.MAY, 29), FieldFactory.parseDate(javaDate));
        assertNull(FieldFactory.parseDate(123));
    }

    @Test
    void parseDateTimeHandlesLocalDateAndFormattedStrings() {
        assertEquals(
                LocalDateTime.of(LocalDate.of(2026, Month.MAY, 29), LocalTime.NOON),
                FieldFactory.parseDateTime(LocalDate.of(2026, Month.MAY, 29))
        );
        assertEquals(
                LocalDateTime.of(2026, Month.MAY, 29, 13, 45),
                FieldFactory.parseDateTimeFromString("29.05.2026 13:45")
        );
        assertEquals(
                LocalDateTime.of(2026, Month.MAY, 29, 13, 45, 10),
                FieldFactory.parseDateTimeFromString("29.05.2026 13:45:10")
        );
    }

    @Test
    void parseDoubleHandlesSupportedTypes() {
        assertEquals(12.5D, FieldFactory.parseDouble("12.5"));
        assertEquals(12D, FieldFactory.parseDouble(12));
        assertEquals(12.5D, FieldFactory.parseDouble(12.5D));
        assertNull(FieldFactory.parseDouble(null));
    }
}
