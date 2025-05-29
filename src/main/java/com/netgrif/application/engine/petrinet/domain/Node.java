package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.utils.UniqueKeyMapWrapper;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
@AllArgsConstructor
public abstract class Node extends ProcessObject {

    private I18nString title;
    private UniqueKeyMapWrapper<String> properties;

    public Node() {
        super();
        this.properties = new UniqueKeyMapWrapper<>();
    }
}
