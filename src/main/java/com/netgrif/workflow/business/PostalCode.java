package com.netgrif.workflow.business;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String code;

    private String city;

    public PostalCode() {
    }

    public PostalCode(String code, String city) {
        this.code = code;
        this.city = city;
    }
}