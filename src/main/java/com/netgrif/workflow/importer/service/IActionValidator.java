package com.netgrif.workflow.importer.service;

public interface IActionValidator extends IModelValidator {
    void validateAction(String action);
}