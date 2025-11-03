package com.netgrif.application.engine.objects.dto.response.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.Position;

import java.io.Serializable;

public record PositionDto(int x, int y) implements Serializable {

    public static PositionDto fromPosition(Position position) {
        return new PositionDto(position.getX(), position.getY());
    }
}
