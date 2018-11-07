package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field

class FieldValidationRunner {

    /**
     * Validation function for field value
     * @param field - field to validate
     * @param rules - validation rules in groovy DSL
     * @return true if value in the field is valid according to validation rules
     */
    static boolean validate(Field field, String rules){
        def shell = new GroovyShell()
        def code = (Closure) shell.evaluate("{-> validate(${rules})}")
        code.delegate = ValidationDelegateFactory.getDelegate(field)
        return code()
    }

    static String toJavascript(Field field, String rules){
        def shell = new GroovyShell()
        def code = (Closure) shell.evaluate("{-> javascript(${rules})}")
        code.delegate = ValidationDelegateFactory.getJavascriptDelegate(field)
        return code()
    }

    @Deprecated
    static String buildJSAlternatives(String rules){
        StringBuilder builder = new StringBuilder()
        rules.split(",").each {rule ->
            if(rule.charAt(0) == '{' as char){
                builder.append(rule.replaceFirst(" ","JS "))
                builder.append(",")
            } else {
                builder.append(rule)
                builder.append("JS,")
            }
        }
        builder.deleteCharAt(builder.length()-1)
        return builder.toString()
    }
}
