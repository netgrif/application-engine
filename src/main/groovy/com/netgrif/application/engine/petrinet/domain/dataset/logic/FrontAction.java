package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class FrontAction {

    private String id;

    private Map<String, Object> args;
}
