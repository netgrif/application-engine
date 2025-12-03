package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.elastic.domain.ActorMappingData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupFieldValue extends ActorFieldValue {

    protected String name;

    public GroupFieldValue(String id, String realmId, String name) {
        super(id, realmId);
        this.name = name;
    }

    public GroupFieldValue(Group group) {
        this(group.getStringId(), group.getRealmId(), group.getDisplayName());
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String toString() {
        return "GroupFieldValue{" + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupFieldValue)) {
            return false;
        }
        return this.id != null && ((GroupFieldValue) obj).getId() != null && this.id.equals(((GroupFieldValue) obj).getId());
    }

    @Override
    public ActorMappingData buildMappingData() {
        return new ActorMappingData(this.id, this.realmId, null, this.getFullName());
    }
}
