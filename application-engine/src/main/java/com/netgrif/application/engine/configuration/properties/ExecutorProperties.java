package com.netgrif.application.engine.configuration.properties;

import lombok.Data;

@Data
public class ExecutorProperties {
    private int size = 500;
    private int timeout = 5;

}
