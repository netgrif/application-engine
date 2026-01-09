package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;


/**
 * Configuration properties for caching in the Netgrif engine.
 * Allows configuration of cache names and additional cache customization.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.cache")
public class CacheConfigurationProperties {

    /**
     * Default cache name for caching Petri nets by their unique ID.
     */
    private String petriNetById = "petriNetById";

    /**
     * Default cache name for caching Petri nets by their identifier.
     */
    private String petriNetByIdentifier = "petriNetByIdentifier";

    /**
     * Default cache name for caching the default versions of Petri nets.
     */
    private String petriNetDefault = "petriNetDefault";

    /**
     * Default cache name for caching the latest versions of Petri nets.
     */
    private String petriNetLatest = "petriNetLatest";

    /**
     * Default cache name for general Petri net caching.
     */
    private String petriNetCache = "petriNetCache";

    /**
     * Default cache name for caching loaded modules in the system.
     */
    private String loadedModules = "loadedModules";

    /**
     * Default cache name for caching global functions of PetriNet global scoped functions.
     */
    private String globalFunctions = "globalFunctions";

    /**
     * A list of additional custom cache names.
     * Allows users to define their own cache names for specific use cases.
     */
    private List<String> additional = new ArrayList<>();

    /**
     * The size of pages used for caching functions when processing large sets of data.
     * This property determines the maximum number of functions to include in a single page during caching operations.
     * Default value is 500.
     */
    private int functionCachingPageSize = 500;

    /**
     * Retrieves a set of all configured cache names.
     * Includes the default caches and any additional user-defined cache names.
     *
     * @return a {@link Set} of all cache names.
     */
    public Set<String> getAllCaches() {
        Set<String> caches = new LinkedHashSet<>(Arrays.asList(petriNetById, petriNetByIdentifier, petriNetDefault,
                petriNetLatest, petriNetCache, loadedModules, globalFunctions));
        caches.addAll(additional);
        return caches;
    }
}
