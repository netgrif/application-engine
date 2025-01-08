package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.FilterField;
import org.springframework.stereotype.Component;

@Component
public class FilterFieldBuilder extends FieldWithAllowedNetsBuilder<FilterField, String> {

    @Override
    public FilterField build(Data data, Importer importer) {
        FilterField field = new FilterField();
        initialize(field);
        setAllowedNets(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.FILTER;
    }
}
