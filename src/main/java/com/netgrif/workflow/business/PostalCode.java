package com.netgrif.workflow.business;

import lombok.Data;
import org.bson.types.ObjectId;


@Data
public class PostalCode {

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