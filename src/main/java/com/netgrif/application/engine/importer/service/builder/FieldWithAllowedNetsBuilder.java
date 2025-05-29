package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.AllowedNets;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldWithAllowedNets;

import java.util.ArrayList;

public abstract class FieldWithAllowedNetsBuilder<T extends FieldWithAllowedNets<U>, U> extends FieldBuilder<T> {

    public void setAllowedNets(T field, Data data) {
        AllowedNets nets = data.getAllowedNets();
        if (nets == null || nets.getAllowedNet() == null || nets.getAllowedNet().isEmpty()) {
            return;
        }
        field.setAllowedNets(new ArrayList<>(nets.getAllowedNet()));
    }
}
