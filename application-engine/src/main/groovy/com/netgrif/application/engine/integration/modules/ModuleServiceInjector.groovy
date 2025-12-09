package com.netgrif.application.engine.integration.modules

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.startup.ApplicationEngineFinishRunner
import com.netgrif.application.engine.startup.annotation.RunnerOrder
import com.netgrif.application.engine.adapter.spring.utils.NaeReflectionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Component responsible for injecting module services into the application runtime.
 * This class implements {@link ApplicationEngineFinishRunner} to perform the injection
 * after the application finishes its startup routine. The services are grouped by modules
 * and made available for runtime action delegation.
 *
 * The execution of this class is controlled by properties defined in the application configuration.
 * By default, it is enabled and executed in the specified order.
 */

@Component
@RunnerOrder(209)
@ConditionalOnProperty(value = "netgrif.engine.module.service.enabled", havingValue = "true", matchIfMissing = true)
class ModuleServiceInjector implements ApplicationEngineFinishRunner {

    private static final Logger log = LoggerFactory.getLogger(ModuleServiceInjector.class)
    private final String DEFAULT_KEY = "default"

    /**
     * Executes the injection of services annotated with {@code @ModuleService}.
     * This method collects all beans marked with the {@code @ModuleService} annotation,
     * groups them by their associated module, and injects them into the respective meta-classes
     * (e.g., {@code ModuleHolder} and {@code ModuleServiceHolder}).
     *
     * @param args application arguments passed during startup
     * @throws Exception if any error occurs during the injection process
     */

    @Override
    void run(ApplicationArguments args) throws Exception {
        def injectableBeans = ApplicationContextProvider.getAppContext().getBeansWithAnnotation(ModuleService.class)
        if (!injectableBeans.isEmpty()) {
            MetaClass holderMetaClass = ModuleHolder.metaClass
            def groupedServices = groupServicesByModule(injectableBeans)
            groupedServices.each { entry ->
                if (entry.key == DEFAULT_KEY) {
                    entry.value.each { serviceEntry ->
                        log.info("Injecting module service {} into action delegate making it available under module.{}", serviceEntry.key, serviceEntry.key)
                        holderMetaClass[serviceEntry.key] = serviceEntry.value
                    }
                } else {
                    MetaClass moduleServiceHolderMetaClass = ModuleServiceHolder.metaClass
                    entry.value.each { serviceEntry ->
                        log.info("Injecting module service {} into module holder {} into action delegate making it available under module.{}.{}", serviceEntry.key, entry.key, entry.key, serviceEntry.key)
                        moduleServiceHolderMetaClass[serviceEntry.key] = serviceEntry.value
                    }
                    holderMetaClass[entry.key] = new ModuleServiceHolder()
                }
            }
        }
    }

    /**
     * Groups services annotated with {@code @ModuleService} by their associated module.
     * Each service is categorized under a module key specified by the annotation's value.
     * Services without a specified module are grouped under the "default" key.
     *
     * @param services a map of service names to service instances retrieved from the application context
     * @return a map where each key represents a module, and the value is another map containing
     *         services (name and instance) associated with that module
     * @throws IllegalStateException if any service lacks the {@code @ModuleService} annotation
     */

    private Map<String, Map<String, Object>> groupServicesByModule(Map<String, Object> services) {
        Map<String, Map<String, Object>> grouped = [(DEFAULT_KEY): [:]]
        services.each { entry ->
            Class serviceClass = NaeReflectionUtils.resolveClass(entry.value)
            ModuleService[] annotations = serviceClass.getAnnotationsByType(ModuleService.class)
            if (annotations.length == 0) throw new IllegalStateException("Module Service bean must have @ModuleService annotations")
            ModuleService annotation = annotations[0]
            if (annotation.value().isBlank()) {
                grouped[(DEFAULT_KEY)].put(entry.key, entry.value)
            } else {
                String moduleName = annotation.value()
                if (!grouped.containsKey(moduleName)) grouped.put(moduleName, [:])
                grouped[moduleName].put(entry.key, entry.value)
            }
        }
        return grouped
    }

}
