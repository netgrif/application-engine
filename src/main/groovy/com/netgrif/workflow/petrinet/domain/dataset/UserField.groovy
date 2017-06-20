package com.netgrif.workflow.petrinet.domain.dataset;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Document
public class UserField extends Field{

    private Set<String> roles;
    @Transient
    private User value;

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

    public UserResource getValue() {
        if(this.value == null) return null;
        return new UserResource(this.value,"small",true);
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public void setValue(Object value) {
        this.value = (User) value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.USER;
    }
}