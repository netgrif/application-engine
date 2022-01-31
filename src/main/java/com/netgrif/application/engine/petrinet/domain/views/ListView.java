package com.netgrif.application.engine.petrinet.domain.views;

import lombok.Data;

@Data
public class ListView extends View {

    private Integer items;

    public ListView(Integer items) {
        this();
        this.items = items;
    }

    public ListView() {
        super("list");
    }
}