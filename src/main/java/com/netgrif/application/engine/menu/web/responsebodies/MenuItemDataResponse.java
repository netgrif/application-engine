package com.netgrif.application.engine.menu.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.DataGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MenuItemDataResponse {

    /**
     * Map containing menu item data where key is the view type and value is a list of immediate fields.
     */
    @Getter
    private final List<DataGroup> data;
}
