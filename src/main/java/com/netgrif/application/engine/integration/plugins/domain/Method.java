package com.netgrif.application.engine.integration.plugins.domain;

import com.netgrif.application.engine.integration.plugins.utils.ClassToStringConverter;
import lombok.Data;
import org.springframework.data.convert.ValueConverter;

import java.util.ArrayList;
import java.util.List;

@Data
public class Method {
    private String name;
    @ValueConverter(ClassToStringConverter.class)
    private List<Class<?>> args;

    public Method() {
        this.args = new ArrayList<>();
    }
}
