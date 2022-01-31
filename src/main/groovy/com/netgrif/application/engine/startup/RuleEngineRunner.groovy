package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.drools.interfaces.IRefreshableKieBase
import com.netgrif.application.engine.configuration.drools.interfaces.IRuleEngineGlobalsProvider
import groovy.text.SimpleTemplateEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class RuleEngineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineRunner)

    @Autowired
    private IRefreshableKieBase refreshableKieBase

    @Autowired
    private IRuleEngineGlobalsProvider sessionInitializer

    @Value('${drools.template.generate:#{true}}')
    private boolean generate

    @Value('${drools.template.path:#{"rules/templates/template.drl"}}')
    private String generatedTemplatePath

    @Value('${drools.template-resource.classpath:#{"rules/templates/template.drl"}}')
    private String templateResource

    @Override
    void run(String... strings) throws Exception {
        log.info("Rule engine runner starting")
        if (generate) {
            log.info("Generating template to " + generatedTemplatePath)
            generateTemplate()
        }

        log.info("Loading rules from database")
        refreshableKieBase.refresh()
        log.info("Rule engine runner finished")
    }

    void generateTemplate() {
        def engine = new SimpleTemplateEngine()
        def binding = [
                imports: sessionInitializer.imports().collect { "$it" }.join(""),
                globals: sessionInitializer.globals().collect { "${it.toString()}" }.join(""),
        ]

        String template = engine.createTemplate(new ClassPathResource(templateResource).inputStream.getText()).make(binding)

        File templateFile = new File(generatedTemplatePath)
        templateFile.getParentFile().mkdirs()
        boolean deleted = templateFile.delete()
        if (!deleted) {
            log.warn("Previous generated template file was not deleted")
        }

        templateFile.createNewFile()
        if (!templateFile.exists()) {
            throw new IllegalStateException("Template file $templateFile.absolutePath was not created")
        }

        templateFile.write(template)

        log.info("Generated template into file ")
    }
}