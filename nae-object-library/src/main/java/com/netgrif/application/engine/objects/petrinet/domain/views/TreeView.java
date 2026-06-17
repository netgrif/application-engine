package com.netgrif.application.engine.objects.petrinet.domain.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TreeView extends View {

    public TreeView() {
        super("tree");
    }
}
