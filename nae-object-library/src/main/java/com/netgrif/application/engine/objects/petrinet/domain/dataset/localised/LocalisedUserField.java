package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedUserField extends LocalisedField {

    private Set<String> roles;

    public LocalisedUserField(ActorField field, Locale locale) {
        super(field, locale);
        this.roles = field.getRoles();
    }
}
