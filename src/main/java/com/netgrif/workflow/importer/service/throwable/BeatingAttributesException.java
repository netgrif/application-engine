package com.netgrif.workflow.importer.service.throwable;

public class BeatingAttributesException extends RuntimeException {

    public BeatingAttributesException(String target, String targetModel, String field1, String field2) {
        super("Attributes \"" + field1 + "\" and \"" + field2 + "\" cannot be present at the same time" +
                " on model \"" + targetModel + "\" with ID \"" + target + "\". Consider using only one of them.");
    }
}
