package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class Node extends PetriNetObject {

    private Position position;

    private I18nString title;

    public Node() {
        this.setObjectId(new ObjectId());
        position = new Position();
    }

    public Node(Node node) {
        this.setObjectId(node.getObjectId());
        this.setPosition(node.getPosition());
        this.setTitle(node.getTitle());
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

    public void setPosition(Position positionData) {
        if (positionData != null) {
            position.setY(positionData.getY());
            position.setX(positionData.getX());
        }
    }

    public void setTitle(I18nString title) {
        this.title = title;
    }

    public void setTitle(String title) {
        setTitle(new I18nString(title));
    }
}
