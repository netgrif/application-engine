package com.netgrif.application.engine.configuration.groovy;

import com.netgrif.application.engine.configuration.properties.ActionsProperties;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GroovyShellConfiguration {

    @Autowired
    private ActionsProperties actionsProperties;

    @Bean
    @ConditionalOnMissingBean
    public ImportCustomizer importCustomizer() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports(getDefaultEngineImports());
        importCustomizer.addImports(actionsProperties.getImports().toArray(new String[0]));
        importCustomizer.addStarImports(actionsProperties.getStarImports().toArray(new String[0]));
        importCustomizer.addStaticStars(actionsProperties.getStaticStarImports().toArray(new String[0]));
        return importCustomizer;
    }

    @Bean
    @ConditionalOnMissingBean
    public CompilerConfiguration compilerConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer());
        return configuration;
    }


    protected String[] getDefaultEngineImports() {
        return new String[]{
                "com.netgrif.application.engine.objects",
                "com.netgrif.application.engine.adapter.spring",
                "java.time"
        };
    }


}
