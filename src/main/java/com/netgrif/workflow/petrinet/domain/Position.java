package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

public class Position {

    @Getter @Setter
    private Integer x;

    @Getter @Setter
    private Integer y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position() {}
}
