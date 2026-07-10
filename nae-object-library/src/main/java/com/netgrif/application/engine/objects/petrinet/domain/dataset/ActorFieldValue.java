package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.elastic.domain.ActorMappingData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActorFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = -6421919316454802530L;

    protected String id;
    protected String realmId;

    public abstract String getFullName();
    public abstract String toString();
    public abstract boolean equals(Object obj);
    public abstract ActorMappingData buildMappingData();

    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
