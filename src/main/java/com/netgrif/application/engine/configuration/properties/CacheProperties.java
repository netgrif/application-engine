package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.cache")
public class CacheProperties {

    private String petriNetById = "petriNetById";

    private String petriNetByIdentifier = "petriNetByIdentifier";

    private String petriNetNewest = "petriNetNewest";

    private String petriNetCache = "petriNetCache";

    private String caseIndexByNodeId = "caseIndexByNodeId";

    private String caseIndexDynamic = "caseIndexDynamic";

    private String caseIndexAll = "caseIndexAll";

    private String caseIndexByMenuTaskId = "caseIndexByMenuTaskId";

    private List<String> additional = new ArrayList<>();

    public Set<String> getAllCaches() {
        List<String> cacheNames = Arrays.asList(
                petriNetById,
                petriNetByIdentifier,
                petriNetNewest,
                petriNetCache,
                caseIndexByNodeId,
                caseIndexDynamic,
                caseIndexAll,
                caseIndexByMenuTaskId
        );

        Set<String> caches = new LinkedHashSet<>(cacheNames);
        caches.addAll(additional);
        return caches;
    }

}
