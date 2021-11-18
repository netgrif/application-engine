package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Logic;
import org.springframework.stereotype.Component;

@Component
public class LogicValidator implements ILogicValidator {
    @Override
    public void checkDeprecatedAttributes(Logic logic) {
        validateAttribute(logic.isAssigned(), "assigned");
    }
}
