package com.netgrif.application.engine.adapter.spring.configuration;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class CustomLevelColorConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String level = String.format("%-5s", event.getLevel().toString());
        return switch (event.getLevel().toString()) {
            case "TRACE" -> AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, level);
            case "DEBUG" -> AnsiOutput.toString(AnsiColor.GREEN, level);
            case "INFO"  -> AnsiOutput.toString(AnsiColor.BLUE, level);
            case "WARN"  -> AnsiOutput.toString(AnsiColor.YELLOW, level);
            case "ERROR" -> AnsiOutput.toString(AnsiColor.RED, level);
            case "FATAL" -> AnsiOutput.toString(AnsiColor.BRIGHT_RED, level);
            default -> level;
        };
    }
}
