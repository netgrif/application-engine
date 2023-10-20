package com.netgrif.application.engine.integration.plugins.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Method {
    private String name;
    private List<String> args;
    public Method() {
        this.args = new ArrayList<>();
    }
}
