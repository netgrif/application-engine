package com.netgrif.application.engine.menu.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.DataGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MenuItemDataResponse {

    /**
     * List of data groups containing menu item data.
     */
    @Getter
    private final List<DataGroup> data;
}
