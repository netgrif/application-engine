package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.AllowedNets;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CaseRefFieldBuilder extends FieldBuilder<CaseField> {
    @Override
    public CaseField build(Data data, Importer importer) {
        AllowedNets nets = data.getAllowedNets();
        CaseField field = new CaseField();
        if (nets != null) {
            field.setAllowedNets(new ArrayList<>(nets.getAllowedNet()));
        }
        setDefaultValues(field, data, ignored -> {
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.CASE_REF;
    }
}
