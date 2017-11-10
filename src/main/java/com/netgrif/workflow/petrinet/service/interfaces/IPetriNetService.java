package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IPetriNetService {

    void importPetriNet(File xmlFile, String name, String initials, LoggedUser user) throws IOException, SAXException, ParserConfigurationException;

    void savePetriNet(PetriNet petriNet);

    PetriNet loadPetriNet(String id);

    List<PetriNet> loadAll();

    List<PetriNetReference> getAllReferences(LoggedUser user);

    List<PetriNetReference> getAllAccessibleReferences(LoggedUser user);

    PetriNetReference getReferenceByTitle(LoggedUser user, String title);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user);

    List<DataFieldReference> getDataFieldReferences(List<String> petriNetIds, List<String> transitionIds);
}
