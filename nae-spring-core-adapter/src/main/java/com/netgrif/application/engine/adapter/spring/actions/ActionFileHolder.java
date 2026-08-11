package com.netgrif.application.engine.adapter.spring.actions;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class ActionFileHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String fileName;

    private byte[] fileContent;
}
