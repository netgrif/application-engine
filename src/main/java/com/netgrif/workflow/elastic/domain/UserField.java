package com.netgrif.workflow.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserField extends DataField {

    private String email;

    private String fullName;

    public UserField(String value, String email, String fullName) {
        super(value);
        this.email = email;
        this.fullName = fullName;
    }
}


