package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Document;
import com.netgrif.application.engine.importer.service.throwable.BeatingAttributesException;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DocumentValidator implements IDocumentValidator {
    @Override
    public void checkDeprecatedAttributes(Document document) {
        validateAttribute(document.getUsersRef(), "usersRef");
    }

    @Override
    public void checkConflictingAttributes(Document document, Object attr1, Object attr2, String attr1Name, String attr2Name) throws BeatingAttributesException {
        if ((attr1 instanceof Collection && attr2 instanceof Collection && !((Collection) attr1).isEmpty() && !((Collection) attr2).isEmpty())) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"document\" with ID \"" + document.getId() + "\". Consider using only one of them.");
        }
        if ((!(attr1 instanceof Collection) && !(attr2 instanceof Collection) && attr1 != null && attr2 != null)) {
            throw new BeatingAttributesException("Attributes \"" + attr1Name + "\" and \"" + attr2Name + "\" cannot be present at the same time" +
                    " on model \"document\" with ID \"" + document.getId() + "\". Consider using only one of them.");
        }
    }
}
