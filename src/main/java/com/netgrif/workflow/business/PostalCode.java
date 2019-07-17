package com.netgrif.workflow.business;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
public class PostalCode implements Serializable {

    private static final long serialVersionUID = 1L;

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