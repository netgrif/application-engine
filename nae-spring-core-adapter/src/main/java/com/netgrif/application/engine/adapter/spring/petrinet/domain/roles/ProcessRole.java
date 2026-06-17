package com.netgrif.application.engine.adapter.spring.petrinet.domain.roles;

import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document
public class ProcessRole extends com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole {

    @Id
    @Override
    public ProcessResourceId get_id() {
        return super.get_id();
    }

    public ProcessRole() {
        super();
    }

    public ProcessRole(String id) {
        super(id);
    }

    public ProcessRole(com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole processRole) {
        super(processRole);
    }
}
