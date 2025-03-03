@QueryEntities({
        com.netgrif.core.workflow.domain.Task.class,
        Task.class,
        com.netgrif.core.workflow.domain.Case.class,
        Case.class,
        com.netgrif.core.petrinet.domain.roles.ProcessRole.class,
        ProcessRole.class,
        com.netgrif.core.petrinet.domain.PetriNet.class,
        PetriNet.class,
        Group.class,
        User.class,
        Actor.class
})
package com.netgrif.application.engine;

import com.netgrif.adapter.petrinet.domain.PetriNet;
import com.netgrif.adapter.petrinet.domain.roles.ProcessRole;
import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.adapter.workflow.domain.Task;
import com.netgrif.core.auth.domain.Actor;
import com.netgrif.core.auth.domain.Group;
import com.netgrif.core.auth.domain.User;
import com.querydsl.core.annotations.QueryEntities;