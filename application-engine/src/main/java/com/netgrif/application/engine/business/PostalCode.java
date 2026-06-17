package com.netgrif.application.engine.business;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;


@Data
public class PostalCode {

    @Id
    private ObjectId _id;

    private String code;

    private String city;

    public PostalCode() {
    }

    public PostalCode(String code, String city) {
        this.code = code;
        this.city = city;
    }
}