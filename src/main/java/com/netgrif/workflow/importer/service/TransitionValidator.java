package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Transition;
import com.netgrif.workflow.importer.service.throwable.BeatingAttributesException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class TransitionValidator implements ITransitionValidator {

    @Override
    public void checkDeprecatedAttributes(Transition transition) {
        validateAttribute(transition.getUsersRef(), "usersRef");
    }

    @Override
    public void checkBeatingAttributes(Transition transition, Object attr1, Object attr2, String attr1Name, String attr2Name) {
        if ((attr1 instanceof Collection && attr2 instanceof Collection && !((Collection) attr1).isEmpty() && !((Collection) attr2).isEmpty())) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"transition\" with ID \"" + transition.getId() + "\". Consider using only one of them.");
        }
        if ((!(attr1 instanceof Collection) && !(attr2 instanceof Collection) && attr1 != null && attr2 != null)) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"transition\" with ID \"" + transition.getId() + "\". Consider using only one of them.");
        }
    }
}
