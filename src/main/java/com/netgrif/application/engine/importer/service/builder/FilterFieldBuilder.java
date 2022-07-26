package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.AllowedNets;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.FilterField;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FilterFieldBuilder extends FieldBuilder<FilterField> {
    @Override
    public FilterField build(Data data, Importer importer) {
        AllowedNets nets = data.getAllowedNets();
        FilterField field = new FilterField();
        if (nets != null) {
            field.setAllowedNets(new ArrayList<>(nets.getAllowedNet()));
        }
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.FILTER;
    }
}
