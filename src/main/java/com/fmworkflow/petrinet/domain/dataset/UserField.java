package com.fmworkflow.petrinet.domain.dataset;


import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
public class UserField extends Field{

    private Set<String> roles;
    @Transient
    private List<String> value;

    public UserField(){
        super();
        this.roles = new HashSet<>();
    }

    public UserField(String[] values){
        this();
        if(values != null){
            this.roles.addAll(Arrays.asList(values));
        }
    }

    public List<String> getValue() {
        return value;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public void setValue(Object value) {
        this.value = (List<String>) value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.USER;
    }
}