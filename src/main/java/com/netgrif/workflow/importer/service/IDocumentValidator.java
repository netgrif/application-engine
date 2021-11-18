package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Document;

public interface IDocumentValidator extends IModelValidator {
    void checkDeprecatedAttributes(Document document);

    void checkBeatingAttributes(Document document, Object attr1, Object attr2, String attr1Name, String attr2Name);
}
