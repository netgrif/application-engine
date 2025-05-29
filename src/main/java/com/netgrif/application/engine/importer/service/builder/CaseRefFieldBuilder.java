package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CaseRefFieldBuilder extends FieldWithAllowedNetsBuilder<CaseField, List<String>> {

    @Override
    public CaseField build(Data data, Importer importer) {
        CaseField field = new CaseField();
        initialize(field);
        setAllowedNets(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.CASE_REF;
    }
}
