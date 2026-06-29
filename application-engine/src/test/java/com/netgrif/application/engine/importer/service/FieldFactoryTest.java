package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class FieldFactoryTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private FieldFactory fieldFactory;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void contextStartsAndFieldFactoryBeanIsAvailable() {
        assertNotNull(testHelper);
        assertNotNull(fieldFactory);
    }

    @Test
    public void parseDateFromStringSupportsExistingLocalDateFormats() {
        LocalDate expected = LocalDate.of(2026, 5, 29);

        assertEquals(expected, FieldFactory.parseDateFromString("20260529"));
        assertEquals(expected, FieldFactory.parseDateFromString("2026-05-29"));
        assertEquals(expected, FieldFactory.parseDateFromString("29.05.2026"));
    }

    @Test
    public void parseDateFromStringUsesUtcForInstantValues() {
        assertEquals(
                LocalDate.of(2026, 5, 29),
                FieldFactory.parseDateFromString("2026-05-29T00:30:00Z")
        );
    }

    @Test
    public void parseDateFromStringKeepsOffsetDateComponent() {
        assertEquals(
                LocalDate.of(2026, 5, 29),
                FieldFactory.parseDateFromString("2026-05-29T00:30:00+02:00")
        );
    }

    @Test
    public void parseDateFromStringReturnsNullForBlankOrInvalidInput() {
        assertNull(FieldFactory.parseDateFromString(null));
        assertNull(FieldFactory.parseDateFromString(" "));
        assertNull(FieldFactory.parseDateFromString("totok"));
        assertNull(FieldFactory.parseDateFromString("not-a-date"));
    }

    @Test
    public void parseDateHandlesDateStringLocalDateAndJavaDateValues() {
        Date javaDate = Date.from(LocalDate.of(2026, 5, 29)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC));

        assertEquals(LocalDate.of(2026, 5, 29), FieldFactory.parseDate("2026-05-29"));
        assertEquals(LocalDate.of(2026, 5, 29), FieldFactory.parseDate(LocalDate.of(2026, 5, 29)));
        assertEquals(LocalDate.of(2026, 5, 29), FieldFactory.parseDate(javaDate));
        assertNull(FieldFactory.parseDate(123));
    }

    @Test
    public void parseDateTimeHandlesLocalDateAndFormattedStrings() {
        assertEquals(
                LocalDateTime.of(LocalDate.of(2026, 5, 29), LocalTime.NOON),
                FieldFactory.parseDateTime(LocalDate.of(2026, 5, 29))
        );
        assertEquals(
                LocalDateTime.of(2026, 5, 29, 13, 45),
                FieldFactory.parseDateTimeFromString("29.05.2026 13:45")
        );
        assertEquals(
                LocalDateTime.of(2026, 5, 29, 13, 45, 10),
                FieldFactory.parseDateTimeFromString("29.05.2026 13:45:10")
        );
    }

    @Test
    public void parseDoubleHandlesSupportedTypes() {
        assertEquals(12.5D, FieldFactory.parseDouble("12.5"));
        assertEquals(12D, FieldFactory.parseDouble(12));
        assertEquals(12.5D, FieldFactory.parseDouble(12.5D));
        assertNull(FieldFactory.parseDouble(null));
    }
}
