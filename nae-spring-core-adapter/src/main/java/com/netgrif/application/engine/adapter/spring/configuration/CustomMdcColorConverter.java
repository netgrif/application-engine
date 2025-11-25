package com.netgrif.application.engine.adapter.spring.configuration;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.NoArgsConstructor;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CustomMdcColorConverter extends ClassicConverter {
    private static final List<String> KEYS = List.of("spanId", "traceId", "realmId", "userId", "username");
    private static final Map<String, AnsiColor> KEY_COLORS = Map.of(
            "spanId", AnsiColor.BLUE,
            "traceId", AnsiColor.BLUE,
            "realmId", AnsiColor.BLUE,
            "userId", AnsiColor.MAGENTA,
            "username", AnsiColor.YELLOW
    );

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        StringBuilder sb = new StringBuilder();

        for (String key : KEYS) {
            String value = mdc.get(key);
            if (value != null && !value.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append("  ");
                }

                sb.append(key).append("=");

                AnsiColor color = KEY_COLORS.getOrDefault(key, AnsiColor.DEFAULT);
                sb.append(AnsiOutput.toString(color, value));
            }
        }

        return sb.toString();
    }

}
