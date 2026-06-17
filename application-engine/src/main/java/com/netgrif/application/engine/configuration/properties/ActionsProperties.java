package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "netgrif.engine.actions")
@Data
@Component
public class ActionsProperties {

    /**
     * Class imports. Example:
     * {@code netgrif.engine.actions.imports=java.time.LocalDate}
     * Will result in: {@code import java.date.LocalDate}
     */
    private List<String> imports = new ArrayList<>();

    /**
     * Package imports. Example:
     * {@code netgrif.engine.actions.star-imports=java.time,com.netgrif}
     * Will result in:
     * {@code import java.date.*
     * import com.netgrif.*}
     */
    private List<String> starImports = new ArrayList<>();

    /**
     * Static property imports. Example:
     * {@code netgrif.engine.actions.static-star-imports=java.time.LocalDate}
     * Will result in: {@code import static java.time.LocalDate.*}
     */
    private List<String> staticStarImports = new ArrayList<>();
}
