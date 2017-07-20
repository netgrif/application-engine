package com.netgrif.workflow.psc;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @Getter @Setter
    private String code;

    @Getter @Setter
    private String locality;

    @Getter @Setter
    private String region;

    @Getter @Setter
    private String regionCode;

    public PostalCode(){}

    public PostalCode(String code) {
        this.code = code;
    }

    public PostalCode(String code, String locality, String region, String regionCode) {
        this.code = code;
        this.locality = locality;
        this.region = region;
        this.regionCode = regionCode;
    }
}
