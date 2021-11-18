package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentValidator implements IDocumentValidator {
    @Override
    public void checkDeprecatedAttributes(Document document) {
        validateAttribute(document.getUsersRef(), "usersRef");
    }
}
