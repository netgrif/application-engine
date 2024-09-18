package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.petrinet.domain.DataRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class LayoutItem {

    private LayoutObjectType layoutType;

    private String dataRefId;

    @Transient
    private DataRef dataRef;

    private LayoutContainer container;

    private Map<String, String> properties;

    public LayoutItem() {
        this.properties = new HashMap<>();
    }

    @Override
    public LayoutItem clone() {
        LayoutItem clone = new LayoutItem();
        clone.setLayoutType(this.layoutType);
        clone.setContainer(this.container != null ? this.container.clone() : null);
        clone.setDataRefId(this.dataRefId);
        clone.setDataRef(this.dataRef == null ? null : this.dataRef.clone());
        clone.setProperties(new HashMap<>(this.properties));
        return clone;
    }
}
