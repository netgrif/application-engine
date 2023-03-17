package com.netgrif.application.engine.rules.domain.facts;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
public abstract class Fact {

    @Id
    private ObjectId id;

    private LocalDateTime creationDate;

    public Fact() {
        id = new ObjectId();
        creationDate = LocalDateTime.now();
    }
}
