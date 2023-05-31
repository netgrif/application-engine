package com.netgrif.application.engine.validation.service;

import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ValidationRegistry {

    private final Map<String, Validation> registry;

    public ValidationRegistry(List<Validation<?>> validators) {
        this.registry = validators.stream().collect(Collectors.toMap(Validation::getName, Function.identity()));
    }

    public void validate(Task task) {
        return;
    }

    public void validate(Transition transition, Case useCase) {
        transition.getDataSet().entrySet();
//        validation.valid(useCase.getPetriNet().getDataSet().get(entry.getKey()), useCase.getDataField(entry.getKey()));
    }


    public void validate(Field<?> field, Case useCase) {
        if (field.getValidations() == null) {
            return;
        }
        field.getValidations().forEach((key, validation) -> {
            if (registry.containsKey(key)) {
                validation.getValidationRule().forEach((ruleKey, rule) -> {
                    if (rule.getDynamic()) {
                        String totok = rule.getRule();
                        try{
                            totok.split(".rawValue");
                            validation.getValidationRule().get(ruleKey).getRule() = useCase.getDataSet().get(totok).getValue().toString();
                        }catch (Exception e){
                            log.warn(e.getMessage());
                        }

                    }
                });
                try {
                    registry.get(key).validate(field);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        });

    }

    public com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation dynamicResolver(com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation validation, Case useCase){
        validation.
    }
}