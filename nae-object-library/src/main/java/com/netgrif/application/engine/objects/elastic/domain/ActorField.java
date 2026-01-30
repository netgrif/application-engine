package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.GroupFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.UserFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ActorField extends DataField {

    protected List<String> usernameValue;
    protected List<String> fullNameValue;
    protected List<String> actorIdValue;
    protected List<String> actorRealmIdValue;

    public ActorField(ActorField field) {
        super(field);
        this.usernameValue = field.usernameValue == null ? null : new ArrayList<>(field.usernameValue);
        this.fullNameValue = field.fullNameValue == null ? null : new ArrayList<>(field.fullNameValue);
        this.actorIdValue = field.actorIdValue == null ? null : new ArrayList<>(field.actorIdValue);
        this.actorRealmIdValue = field.actorRealmIdValue == null ? null : new ArrayList<>(field.actorRealmIdValue);
    }

    public ActorField(ActorMappingData mappingData) {
        this(List.of(mappingData));
    }

    public ActorField(List<ActorMappingData> mappingDataList) {
        super();
        if (mappingDataList == null || mappingDataList.isEmpty()) {
            return;
        }

        this.usernameValue = new ArrayList<>();
        this.fullNameValue = new ArrayList<>();
        this.actorIdValue = new ArrayList<>();
        this.actorRealmIdValue = new ArrayList<>();
        for (ActorMappingData mappingData : mappingDataList) {
            this.fullNameValue.add(mappingData.fullName());
            this.actorIdValue.add(mappingData.actorId());
            this.actorRealmIdValue.add(mappingData.actorRealmId());
            if (mappingData.username() == null) {
                this.fulltextValue.add(mappingData.fullName());
            } else {
                this.usernameValue.add(mappingData.username());
                super.fulltextValue.add(String.format("%s %s", mappingData.fullName(), mappingData.username()));
            }
        }
    }

    @Override
    public Object getValue() {
        if (this.actorIdValue != null && this.actorIdValue.size() == 1) {
            return buildFieldValue(0);
        } else if (this.actorIdValue != null && this.actorIdValue.size() > 1) {
            return IntStream.range(0, this.actorIdValue.size()).mapToObj(this::buildFieldValue).toList();
        }
        return null;
    }

    protected ActorFieldValue buildFieldValue(int idx) {
        String username = this.usernameValue.get(idx);
        if (username == null) {
            return new GroupFieldValue(this.actorIdValue.get(idx), this.actorRealmIdValue.get(idx), this.fullNameValue.get(idx));
        } else {
            String fullName = this.fullNameValue.get(idx) != null ? this.fullNameValue.get(idx) : "";
            String[] fullNameSplit = fullName.split(" ", 2);
            String firstName = fullNameSplit.length > 0 ? fullNameSplit[0] : "";
            String lastName = fullNameSplit.length > 1 ? fullNameSplit[1] : "";
            return new UserFieldValue(this.actorIdValue.get(idx), this.actorRealmIdValue.get(idx), firstName, lastName, username);
        }
    }
}


