package com.fmworkflow.petrinet.domain;

import com.fmworkflow.Persistable;

public abstract class Editable2D implements Persistable {
    private Position position;

    public Editable2D() {
        position = new Position();
    }

    public Position getPosition() {
        return position;
    }

    public void setPositionX(int x) {
        position.setX(x);
    }

    public void setPositionY(int y) {
        position.setY(y);
    }
}
