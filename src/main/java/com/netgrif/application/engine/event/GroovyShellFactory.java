package com.netgrif.application.engine.event;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroovyShellFactory implements IGroovyShellFactory {

    private final CompilerConfiguration configuration;

    public GroovyShellFactory(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public GroovyShell getGroovyShell() {
        return new GroovyShell(this.getClass().getClassLoader(), this.configuration);
    }
}
