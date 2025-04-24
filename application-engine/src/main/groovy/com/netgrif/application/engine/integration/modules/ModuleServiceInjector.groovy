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

    @Override
    void run(ApplicationArguments args) throws Exception {
        def injectableBeans = ApplicationContextProvider.getAppContext().getBeansWithAnnotation(ModuleService.class)
        if (!injectableBeans.isEmpty()) {
            MetaClass holderMetaClass = ModuleHolder.metaClass
            injectableBeans.forEach((key, value) -> {
                log.debug("Injecting module service {} into action delegate make it available under module.{}", key, key)
                holderMetaClass[key] = value
            })
        }
    }
}
