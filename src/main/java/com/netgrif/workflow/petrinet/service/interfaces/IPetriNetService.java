package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetSmall;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;

public interface IPetriNetService {

    Optional<PetriNet> importPetriNetAndDeleteFile(File xmlFile, UploadedFileMeta netMetaData, LoggedUser user) throws IOException;

    Optional<PetriNet> importPetriNet(File xmlFile, UploadedFileMeta metaData, LoggedUser user) throws IOException;

    void savePetriNet(PetriNet petriNet);

    PetriNet loadPetriNet(String id);

    List<PetriNet> loadAll();

    PetriNet getNewestByIdentifier(String identifier);

    List<PetriNetReference> getAllReferences(LoggedUser user, Locale locale);

    List<PetriNetReference> getAllAccessibleReferences(LoggedUser user, Locale locale);

    PetriNetReference getReferenceByTitle(LoggedUser user, String title, Locale locale);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    Page<PetriNetSmall> searchPetriNet(Map<String, Object> criteria, LoggedUser user, Pageable pageable, Locale locale);

    List<DataFieldReference> getDataFieldReferences(List<String> petriNetIds, List<String> transitionIds, Locale locale);

    FileSystemResource getNetFile(String netId, StringBuilder title);
}
