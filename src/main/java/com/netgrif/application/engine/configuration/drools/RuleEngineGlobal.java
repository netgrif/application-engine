package com.netgrif.application.engine.configuration.drools;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleEngineGlobal {

    private String packageName;

    private String className;

    private String globalName;

    private Object injected;

    public static RuleEngineGlobal engineGlobal(String packageName, String className, String globalName, Object injected) {
        return new RuleEngineGlobal(packageName, className, globalName, injected);
    }

    public static RuleEngineGlobal engineGlobal(String globalName, Object injected) {
        return new RuleEngineGlobal(
                injected.getClass().getPackage().getName(),
                injected.getClass().getSimpleName(),
                globalName, injected
        );
    }

    public String fullyQualifiedName() {
        return packageName + "." + className;
    }

    @Override
    public String toString() {
        return "global " + className + " " + globalName + ";\n";
    }

}
