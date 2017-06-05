package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

public class Position {

    @Getter
    @Setter
    private int x;

    @Getter
    @Setter
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position() {}
}
