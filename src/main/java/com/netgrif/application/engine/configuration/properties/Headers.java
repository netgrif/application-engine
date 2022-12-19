package com.netgrif.application.engine.configuration.properties;

import lombok.Data;

import java.util.List;

@Data
public class Headers {

    /**
     * Allowed HOST in HTTP header
     */
    private List<String> hostAllowed;

}
