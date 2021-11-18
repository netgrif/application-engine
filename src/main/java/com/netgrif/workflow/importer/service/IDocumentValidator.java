package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Document;

public interface IDocumentValidator extends IModelValidator {
    void checkDeprecatedAttributes(Document document);
}
