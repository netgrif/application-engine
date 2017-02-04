package com.fmworkflow.petrinet.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class Node extends PetriNetObject {
    private Position position;
    private String title;

    public Node() {
        this.setObjectId(new ObjectId());
        position = new Position();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setPosition(int x, int y) {
        position.setX(x);
        position.setY(y);
    }
}
