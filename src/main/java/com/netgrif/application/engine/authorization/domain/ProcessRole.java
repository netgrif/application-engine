package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.service.factory.ProcessRoleAssignmentFactory;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(collection = "role")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProcessRole extends Role {

    public static final String DEFAULT_ROLE = "default";
    public static final String ANONYMOUS_ROLE = "anonymous";

    private I18nString title;
    private I18nString description;

    public ProcessRole(ObjectId id, String importId) {
        super(id);
        this.importId = importId;
        this.events = new HashMap<>();
    }

    public ProcessRole(String importId) {
        this(new ObjectId(), importId);
    }

    public ProcessRole() {
        this(new ObjectId(), null);
    }

    @Override
    public Class<?> getAssignmentFactoryClass() {
        return ProcessRoleAssignmentFactory.class;
    }

    @Override
    public String getTitleAsString() {
        return this.title != null ? this.title.getDefaultValue() : this.importId;
    }

    @EqualsAndHashCode.Include
    public String getStringId() {
        return id.toString();
    }

    public void setStringId(String id) {
        this.id = new ObjectId(id);
    }

    public String getLocalisedName(Locale locale) {
        if (title == null) {
            return null;
        }
        return title.getTranslation(locale);
    }

    @Override
    public String toString() {
        return getTitleAsString();
    }

    @Override
    public ProcessRole clone() {
        ProcessRole clone = new ProcessRole(this.importId);
        clone.setStringId(this.getStringId());
        clone.setTitle(this.title == null ? null : this.title.clone());
        clone.setDescription(this.description == null ? null : this.description.clone());
        return clone;
    }
}