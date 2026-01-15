package com.netgrif.application.engine.adapter.spring.configuration;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.List;
import java.util.Map;


public class CustomMdcConverter extends ClassicConverter {

    private record KeyLength(String key, int length) {
    }

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final List<KeyLength> KEYS = List.of(
            new KeyLength(TRACE_ID, 40),
            new KeyLength(USER_ID, 31));

    private static final Map<String, AnsiColor> KEY_COLORS = Map.of(
            TRACE_ID, AnsiColor.MAGENTA,
            USER_ID, AnsiColor.BLUE
    );

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        StringBuilder sb = new StringBuilder();

        for (KeyLength keyLength : KEYS) {
            String key = keyLength.key();
            int length = keyLength.length();
            String value = mdc.get(key);
            appendColorValueOrEmptySpace(sb, key, value, length);
            sb.append(" ");
        }
        return sb.toString();
    }

    private void appendColorValueOrEmptySpace(StringBuilder sb, String key, String value, int length) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null || value.isEmpty()) {
            sb.append(fillEmptySpaces(length));
        } else {
            sb.append(key).append("=");
            AnsiColor color = KEY_COLORS.getOrDefault(key, AnsiColor.DEFAULT);
            sb.append(AnsiOutput.toString(color, value));
        }
    }

    private String fillEmptySpaces(int length) {
        return " ".repeat(length);
    }

}
