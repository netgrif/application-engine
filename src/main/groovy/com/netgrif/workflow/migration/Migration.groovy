package com.netgrif.workflow.migration

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDateTime

@Document
class Migration {

    @Id
    private ObjectId id

    private LocalDateTime runDateTime

    private String title

    Migration(String title) {
        this.title = title
        this.runDateTime = LocalDateTime.now()
    }
}