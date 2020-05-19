package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public abstract class Fact {

    @Id
    private ObjectId _id;
}
