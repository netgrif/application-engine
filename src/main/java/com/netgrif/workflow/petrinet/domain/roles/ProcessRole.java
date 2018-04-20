package com.netgrif.workflow.petrinet.domain.roles;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.Imported;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;

@Document
@Getter
public class ProcessRole extends Imported {

    public static final String DEFAULT_ROLE = "default";

    @Id
    @Setter
    private ObjectId _id;

    private I18nString name;

    @Setter
    private String description;

    public ProcessRole() {
        _id = new ObjectId();
    }

    public String getStringId() {
        return _id.toString();
    }

    public void setName(String name) {
        setName(new I18nString(name));
    }

    public void setName(I18nString name) {
        this.name = name;
    }

    public String getLocalisedName(Locale locale) {
        if (name == null)
            return null;
        return name.getTranslation(locale);
    }

    @Override
    public String toString() {
        return name.getDefaultValue();
    }
}