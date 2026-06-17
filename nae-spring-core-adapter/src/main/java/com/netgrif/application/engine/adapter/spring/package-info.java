@QueryEntities({
        com.netgrif.application.engine.objects.workflow.domain.Task.class,
        Task.class,
        com.netgrif.application.engine.objects.workflow.domain.Case.class,
        Case.class,
        com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole.class,
        ProcessRole.class,
        com.netgrif.application.engine.objects.petrinet.domain.PetriNet.class,
        PetriNet.class,
        Group.class,
        User.class
})
package com.netgrif.application.engine.adapter.spring;

import com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.adapter.spring.workflow.domain.Case;
import com.netgrif.application.engine.adapter.spring.workflow.domain.Task;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.querydsl.core.annotations.QueryEntities;
