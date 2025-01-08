package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.ProcessObject;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
@AllArgsConstructor
public abstract class Node extends ProcessObject {

    private I18nString title;
    private UniqueKeyMap<String, String> properties;

    public Node() {
        super();
        this.properties = new UniqueKeyMap<>();
    }
}
