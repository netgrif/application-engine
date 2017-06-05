package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class Node extends PetriNetObject {

    @Getter
    private Position position;

    @Getter
    @Setter
    private String title;

    public Node() {
        this.setObjectId(new ObjectId());
        position = new Position();
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
