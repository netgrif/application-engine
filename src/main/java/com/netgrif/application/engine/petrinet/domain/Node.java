package com.netgrif.application.engine.petrinet.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Node extends ProcessObject {

    private I18nString title;
}
