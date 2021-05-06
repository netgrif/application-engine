package com.netgrif.workflow.migration

import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

import java.util.stream.Collectors

@Component
@Slf4j
class ActionMigration {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IUserService userService;

    void migrateActions(String petriNetPath){
        InputStream netStream = new ClassPathResource(petriNetPath).inputStream
        Optional<PetriNet> newPetriNet = petriNetService.importPetriNet(netStream, "major", userService.loggedOrSystem.transformToLoggedUser())
        List<PetriNet> oldPetriNets

        if(!newPetriNet.isPresent()) {
            String message = "Petri net from file [" + petriNetPath + "] was not imported"
            log.error(message)
            throw new IllegalArgumentException(message)
        } else {
            oldPetriNets = petriNetService.getByIdentifier(newPetriNet.get().importId)
                    .stream().filter({ net -> (net.version != newPetriNet.get().version)})
                    .collect(Collectors.toList())
        }

        if(oldPetriNets.size() == 0){
            String message = "Older version of Petri net with ID [" + newPetriNet.get().importId + "] is not present in MongoDB."
            log.error(message)
            throw new IllegalArgumentException(message)
        } else {
            oldPetriNets.each {net ->
                migrateDataSetActions(newPetriNet.get(), net)
                migrateDataRefActions(newPetriNet.get(), net)
                petriNetService.save(net)
            }
        }
    }

    private void migrateDataSetActions(PetriNet newPetriNet, PetriNet oldPetriNet){
            newPetriNet.dataSet.each {key, data ->
                if (data.events != null && data.events.size() > 0){
                    oldPetriNet.dataSet[key].events = data.events
                }
            }
    }

    private void migrateDataRefActions(PetriNet newPetriNet, PetriNet oldPetriNet){
        newPetriNet.transitions.each {transKey, trans ->
           trans.dataSet.each {dataKey, data ->
               if(data.events != null && data.events.size() > 0){
                   oldPetriNet.transitions[transKey].dataSet[dataKey].events = data.events
               }
           }
        }
    }

}
