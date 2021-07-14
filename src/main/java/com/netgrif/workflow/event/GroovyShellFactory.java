package com.netgrif.workflow.event;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroovyShellFactory implements IGroovyShellFactory {

    @Autowired
    CompilerConfiguration configuration;

    @Override
    public GroovyShell getGroovyShell() {
        return new GroovyShell(this.getClass().getClassLoader(), this.configuration);
    }
}
