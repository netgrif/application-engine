package com.netgrif.application.engine.serialization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Setter
@Getter
@Component
@RequestScope
public class FieldSelectorHolder {
    private FieldSelector selector = FieldSelector.parse(null);
}
