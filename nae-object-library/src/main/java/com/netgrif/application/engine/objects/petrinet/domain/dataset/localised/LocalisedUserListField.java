package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorListField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedUserListField extends LocalisedField {

    private Set<String> roles;

    public LocalisedUserListField(ActorListField field, Locale locale) {
        super(field, locale);
        this.roles = field.getRoles();
    }
}
