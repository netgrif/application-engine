package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Position implements Serializable {

    @Serial
    private static final long serialVersionUID = 4514035625907226577L;

    private Integer x;

    private Integer y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
    }

    @Override
    public Position clone() {
        Position clone = new Position();
        clone.setX(this.x);
        clone.setY(this.y);
        return clone;
    }
}
