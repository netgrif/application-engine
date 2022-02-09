package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "nae.actions")
@Data
@Component
public class ActionsProperties {

    /**
     * <pre>
     * Class imports. Example:
     * <code>nae.actions.imports=java.time.LocalDate</code>
     * Will result in:
     * <code>import java.date.LocalDate</code>
     * </pre>
     */
    private List<String> imports = new ArrayList<>();

    /**
     * <pre>
     * Package imports. Example:
     * <code>nae.actions.star-imports=java.time,com.netgrif</code>
     * Will result in:
     * <code>import java.date.*
     * import com.netgrif.*</code>
     * </pre>
     */
    private List<String> starImports = new ArrayList<>();

    /**
     * <pre>
     * Static property imports. Example:
     * <code>nae.actions.static-star-imports=java.time.LocalDate</code>
     * Will result in:
     * <code>import static java.time.LocalDate.*</code>
     * </pre>
     */
    private List<String> staticStarImports = new ArrayList<>();
}
