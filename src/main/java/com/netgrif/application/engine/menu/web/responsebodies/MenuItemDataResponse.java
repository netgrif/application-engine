package com.netgrif.application.engine.menu.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MenuItemDataResponse {

    /**
     * Map containing menu item data where key is the view type and value is a list of immediate fields.
     */
    @Getter
    private final Map<String, List<Field<?>>> data;
    
    public MenuItemDataResponse() {
        this(new HashMap<>());
    }
}
