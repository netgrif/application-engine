package com.netgrif.application.engine.event;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroovyShellFactory implements IGroovyShellFactory {

    @Autowired
    private CompilerConfiguration configuration;

    @Override
    public GroovyShell getGroovyShell() {
        ImportCustomizer importCustomizer = new ImportCustomizer();

        Set<String> classNames = findAllClassesUsingClassLoader("com.netgrif.application.engine.workflow.domain");
        importCustomizer.addImports(classNames.toArray(new String[0]));

        configuration.addCompilationCustomizers(importCustomizer);

        return new GroovyShell(this.getClass().getClassLoader(), new groovy.lang.Binding(), this.configuration);
    }

    private Set<String> findAllClassesUsingClassLoader(String packageName) {
        String path = packageName.replace(".", "/");
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        if (stream == null) {
            return Set.of();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines()
                    .filter(line -> line.endsWith(".class"))
                    .map(line -> packageName + "." + line.substring(0, line.lastIndexOf('.')))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
