package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class MenuItemView {
    @NonNull
    private final I18nString name;
    @NonNull
    private final String identifier;
    /**
     * List of view identifiers of views, that can be associated with the view
     * */
    private final List<String> allowedAssociatedViews;
    private final boolean isTabbed;
    /**
     * if false, the view cannot be used as first configuration of the menu_item, but can be used as secondary
     * (associated to another view)
     * */
    private final boolean isPrimary;
}
