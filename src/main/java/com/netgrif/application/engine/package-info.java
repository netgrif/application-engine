@QueryEntities({
        Task.class,
        com.netgrif.core.workflow.domain.Case.class,
        Case.class,
        PetriNet.class,
        User.class
})
package com.netgrif.application.engine;

import com.netgrif.adapter.petrinet.domain.PetriNet;
import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.adapter.workflow.domain.Task;
import com.netgrif.core.auth.domain.User;
import com.querydsl.core.annotations.QueryEntities;