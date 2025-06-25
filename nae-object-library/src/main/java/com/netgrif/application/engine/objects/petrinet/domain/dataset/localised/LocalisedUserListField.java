package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.UserListField;
import lombok.Data;

import java.util.Locale;
import java.util.Set;

@Data
public class LocalisedUserListField extends LocalisedField {

    private Set<String> roles;

    public LocalisedUserListField(UserListField field, Locale locale) {
        super(field, locale);
        this.roles = field.getRoles();
    }
}
