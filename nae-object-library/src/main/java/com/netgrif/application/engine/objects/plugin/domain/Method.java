package com.netgrif.application.engine.objects.plugin.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain class for plugin methods. This domain represents methods, that are implemented inside entry points
 * and are annotated with {@code com.netgrif.application.engine.adapter.spring.plugin.annotations.EntryPointMethod} annotations. These methods
 * can be run from server where the plugin is registered.
 * */
@Data
public class Method implements Serializable {
    @Serial
    private static final long serialVersionUID = -5007466888924505057L;

    private String name;
    private List<String> argTypes;
    private String returnType;
    private List<ListenerFilter> listenerFilters;

    public Method() {
        argTypes = new ArrayList<>();
        listenerFilters = new ArrayList<>();
    }

    @Builder
    public Method(String name, List<String> argTypes, String returnType, List<ListenerFilter> listenerFilters) {
        this.name = name;
        this.argTypes = argTypes;
        this.returnType = returnType;
        this.listenerFilters = listenerFilters;
    }
}
