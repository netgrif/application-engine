package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Logic;
import com.netgrif.application.engine.importer.model.Transition;
import com.netgrif.application.engine.importer.service.throwable.BeatingAttributesException;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class LogicValidator implements ILogicValidator {
    @Override
    public void checkDeprecatedAttributes(Logic logic) {
        validateAttribute(logic.isAssigned(), "assigned");
    }

    @Override
    public void checkConflictingAttributes(Logic logic, Object attr1, Object attr2, String attr1Name, String attr2Name) throws BeatingAttributesException {
        if ((attr1 instanceof Collection && attr2 instanceof Collection && !((Collection) attr1).isEmpty() && !((Collection) attr2).isEmpty())) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"logic\". Consider using only one of them.");
        }
        if ((!(attr1 instanceof Collection) && !(attr2 instanceof Collection) && attr1 != null && attr2 != null)) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"logic\". Consider using only one of them.");
        }
    }
}
