package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.workflow.domain.CaseElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class LayoutContainer extends CaseElement {

    private LayoutObjectType layoutType;

    private List<LayoutItem> items;

    private Map<String, String> properties;

    @Transient
    private String parentTaskId;

    @Transient
    private String parentTransitionId;

    @Transient
    private String parentCaseId;

    public LayoutContainer() {
        this.items = new LinkedList<>();
        this.properties = new HashMap<>();
    }

    public LayoutContainer(LayoutObjectType layoutType) {
        this();
        this.layoutType = layoutType;
    }

    public void addLayoutItem(LayoutItem layoutItem) {
        this.items.add(layoutItem);
    }

    @Override
    public LayoutContainer clone() {
        LayoutContainer clone = new LayoutContainer();
        clone.setLayoutType(this.layoutType);
        clone.setItems(this.items == null ? null : this.items.stream().map(LayoutItem::clone).collect(Collectors.toList()));
        clone.setProperties(new HashMap<>(this.properties));
        return clone;
    }
}
