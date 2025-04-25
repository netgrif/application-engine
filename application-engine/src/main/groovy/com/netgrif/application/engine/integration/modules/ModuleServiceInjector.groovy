package com.netgrif.application.engine.integration.modules

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.startup.ApplicationEngineFinishRunner
import com.netgrif.application.engine.startup.annotation.RunnerOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@RunnerOrder(209)
@ConditionalOnProperty(value = "nae.modules.services.enabled", havingValue = "true", matchIfMissing = true)
class ModuleServiceInjector implements ApplicationEngineFinishRunner {

    private static final Logger log = LoggerFactory.getLogger(ModuleServiceInjector.class)
    private final String DEFAULT_KEY = "default"

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

    private Map<String, Map<String, Object>> groupServicesByModule(Map<String, Object> services) {
        Map<String, Map<String, Object>> grouped = [(DEFAULT_KEY): [:]]
        services.each { entry ->
            ModuleService[] annotations = entry.value.getClass().getAnnotationsByType(ModuleService.class)
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
