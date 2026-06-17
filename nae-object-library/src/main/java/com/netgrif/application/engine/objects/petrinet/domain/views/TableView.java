package com.netgrif.application.engine.objects.petrinet.domain.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TableView extends View {

    public TableView() {
        super("table");
    }
}
