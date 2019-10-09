package com.netgrif.workflow.admin

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class AdminConsoleRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminConsoleRunner.class)


    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDeleget()

    Object run(String action) {

        log.debug("Action: $action")
//        Binding binding = new Binding();
//        GroovyShell shell = new GroovyShell(binding);
        def code = getActionCode(action)
//        Object result = shell.evaluate(action);
        return code()
    }

    private Closure getActionCode(String action) {
        def code = (Closure) new GroovyShell().evaluate("{-> ${action}}")
         code.delegate = getActionDeleget()
        return code
    }


//    private void parse(InputStream script, Binding binding) {
//        if (script==null)
//            throw new IllegalArgumentException("No script is provided");
//        setBinding(binding);
//        CompilerConfiguration cc = new CompilerConfiguration();
//        cc.setScriptBaseClass(ClosureScript.class.getName());
//        GroovyShell shell = new GroovyShell(classLoader,binding,cc);
////        ClosureScript s = (ClosureScript)shell.parse(new InputStreamReader(script));
//        s.setDelegate(this);
//        s.run();
//    }

}
