package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Document;
import com.netgrif.workflow.importer.service.throwable.BeatingAttributesException;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DocumentValidator implements IDocumentValidator {
    @Override
    public void checkDeprecatedAttributes(Document document) {
        validateAttribute(document.getUsersRef(), "usersRef");
    }

    @Override
    public void checkBeatingAttributes(Document document, Object attr1, Object attr2, String attr1Name, String attr2Name) {
        if ((attr1 instanceof Collection && attr2 instanceof Collection && !((Collection) attr1).isEmpty() && !((Collection) attr2).isEmpty())) {
            throw new BeatingAttributesException(document.getId(), "process", attr1Name, attr2Name);
        }
        if ((!(attr1 instanceof Collection) && !(attr2 instanceof Collection) && attr1 != null && attr2 != null)) {
            throw new BeatingAttributesException(document.getId(), "process", attr1Name, attr2Name);
        }
    }
}
