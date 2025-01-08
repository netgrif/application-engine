package com.netgrif.application.engine.elastic.service.transform;


import com.netgrif.application.engine.elastic.domain.DataField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.Field;

public abstract class ElasticDataFieldTransformer<T extends Field<?>, U extends DataField> {

    public abstract U transform(T caseField, T petriNetField);

    public abstract DataType getType();
}
