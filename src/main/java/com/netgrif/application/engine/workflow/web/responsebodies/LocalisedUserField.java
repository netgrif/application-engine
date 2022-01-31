package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import lombok.Data;

import java.util.Locale;
import java.util.Set;

@Data
public class LocalisedUserField extends LocalisedField {

    private Set<String> roles;

    public LocalisedUserField(UserField field, Locale locale) {
        super(field, locale);
        this.roles = field.getRoles();
    }
}