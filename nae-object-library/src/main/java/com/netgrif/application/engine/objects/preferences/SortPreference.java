package com.netgrif.application.engine.objects.preferences;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SortPreference implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String headerUniqueId;
    private String sortDirection;
}
