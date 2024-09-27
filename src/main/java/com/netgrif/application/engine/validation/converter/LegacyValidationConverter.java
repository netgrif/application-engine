package com.netgrif.application.engine.validation.converter;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Argument;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class LegacyValidationConverter {

    public Validation convert(com.netgrif.application.engine.importer.model.Validation validation) {
        Validation converted = new Validation();
        String name = getValidationName(validation.getExpression().getValue());
        Map<String, Argument> arguments = getValidationArguments(validation.getExpression().getValue(), validation.getExpression().isDynamic());
        if (name != null) {
            converted.setName(name);
        }
        if (arguments != null) {
            converted.setArguments(arguments);
        }
        return converted;
    }

    public Validation convert(Validation validation) {
        if (validation.getName() != null || (validation.getArguments() != null && validation.getArguments().size() > 0)) {
            return validation;
        }
        Validation converted = new Validation();
        String name = getValidationName(validation.getValidationRule());
        Map<String, Argument> arguments = getValidationArguments(validation.getValidationRule(), validation instanceof DynamicValidation);
        if (name != null) {
            converted.setName(name);
        }
        if (arguments != null) {
            converted.setArguments(arguments);
        }
        return converted;
    }

    private String getValidationName(String rule) {
        if (rule.startsWith("minlength")) {
            return "minlength";
        } else if (rule.startsWith("maxlength")) {
            return "maxlength";
        } else if (rule.startsWith("regex")) {
            return "regex";
        } else if (rule.startsWith("telnumber")) {
            return "telnumber";
        } else if (rule.startsWith("email")) {
            return "email";
        }  else if (rule.startsWith("between")) {
            return "between";
        } else if (rule.startsWith("workday")) {
            return "workday";
        } else if (rule.startsWith("weekend")) {
            return "weekend";
        } else if (rule.startsWith("requiredTrue")) {
            return "requiredTrue";
        } else if (rule.startsWith("translationRequired")) {
            return "translationRequired";
        } else if (rule.startsWith("translationOnly")) {
            return "translationOnly";
        } else if (rule.startsWith("odd")) {
            return "odd";
        } else if (rule.startsWith("even")) {
            return "even";
        } else if (rule.startsWith("positive")) {
            return "positive";
        } else if (rule.startsWith("negative")) {
            return "negative";
        } else if (rule.startsWith("decimal")) {
            return "decimal";
        } else if (rule.startsWith("inrange")) {
            return "inrange";
        }
        return null;
    }

    private Map<String, Argument> getValidationArguments(String rule, boolean isDynamic) {
        if (rule.startsWith("minlength")) {
            return Collections.singletonMap("length", Argument.builder().name("length").dynamic(isDynamic).value(fillIfDynamic(rule.replace("minlength ", ""), isDynamic)).build());
        } else if (rule.startsWith("maxlength")) {
            return Collections.singletonMap("length", Argument.builder().name("length").dynamic(isDynamic).value(fillIfDynamic(rule.replace("maxlength ", ""), isDynamic)).build());
        } else if (rule.startsWith("regex")) {
            if(rule.startsWith("regex(")) {
                return Collections.singletonMap("expression", Argument.builder().name("expression").dynamic(isDynamic).value(fillIfDynamic(rule.substring(7, rule.length() - 2), isDynamic)).build());
            }
            return Collections.singletonMap("expression", Argument.builder().name("expression").dynamic(isDynamic).value(fillIfDynamic(rule.substring(6), isDynamic)).build());
        } else if (rule.startsWith("between")) {
            String[] fromTo = rule.replace("between ", "").split(",");
            return new HashMap<>(){{
                put("from", Argument.builder().name("from").dynamic(isDynamic).value(fillIfDynamic(fromTo[0], isDynamic)).build());
                put("to", Argument.builder().name("to").dynamic(isDynamic).value(fillIfDynamic(fromTo[1], isDynamic)).build());
            }};
        } else if (rule.startsWith("translationRequired")) {
            return Collections.singletonMap("languages", Argument.builder().name("languages").dynamic(isDynamic).value(fillIfDynamic(rule.replace("translationRequired ", ""), isDynamic)).build());
        } else if (rule.startsWith("translationOnly")) {
            return Collections.singletonMap("languages", Argument.builder().name("languages").dynamic(isDynamic).value(fillIfDynamic(rule.replace("translationOnly ", ""), isDynamic)).build());
        } else if (rule.startsWith("inrange")) {
            String[] fromTo =  rule.replace("inrange ", "").split(",");
            return new HashMap<>(){{
                put("from", Argument.builder().name("from").dynamic(isDynamic).value(fillIfDynamic(fromTo[0], isDynamic)).build());
                put("to", Argument.builder().name("to").dynamic(isDynamic).value(fillIfDynamic(fromTo[1], isDynamic)).build());
            }};
        }
        return null;
    }

    private String fillIfDynamic(String expression, boolean isDynamic) {
        return isDynamic ? "\"" + expression + "\"" : expression;
    }
}
