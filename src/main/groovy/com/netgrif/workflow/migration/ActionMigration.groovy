package com.netgrif.workflow.migration

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ActionMigration extends MigrationOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService


    @Override
    void migrate() {
        List<PetriNet> allNets = petriNetService.getAll()
    }
}
