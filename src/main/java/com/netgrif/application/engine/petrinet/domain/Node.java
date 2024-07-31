package com.netgrif.application.engine.petrinet.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
public abstract class Node extends ProcessObject {

    private Position position;
    private I18nString title;

    public Node() {
        position = new Position();
    }

    public void setPosition(int x, int y) {
        position.setX(x);
        position.setY(y);
    }

    public void setPosition(Position positionData) {
        if (positionData == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        position.setY(positionData.getY());
        position.setX(positionData.getX());
    }
}
