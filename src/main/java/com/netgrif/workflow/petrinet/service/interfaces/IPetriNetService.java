package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import org.bson.types.ObjectId;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface IPetriNetService {

    Optional<PetriNet> importPetriNetAndDeleteFile(File xmlFile, UploadedFileMeta netMetaData, LoggedUser user) throws IOException;

    Optional<PetriNet> importPetriNet(File xmlFile, UploadedFileMeta metaData, LoggedUser user) throws IOException;

    Optional<PetriNet> saveNew(PetriNet petriNet);

    PetriNet getPetriNet(String id);

    PetriNet getPetriNet(String identifier, String version);

    List<PetriNet> getByIdentifier(String identifier);

    PetriNet getNewestVersionByIdentifier(String identifier);

    List<PetriNet> getAll();

    FileSystemResource getFile(String netId, String title);

    List<PetriNetReference> getReferences(LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByVersion(String version, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByUsersProcessRoles(LoggedUser user, Locale locale);

    PetriNetReference getReference(String identifier, String version, LoggedUser user, Locale locale);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale);

    Page<PetriNetReference> search(Map<String, Object> criteria, LoggedUser user, Pageable pageable, Locale locale);

    Optional<PetriNet> findByImportId(long id);

    static PetriNetReference transformToReference(PetriNet net, Locale locale) {
        //return new PetriNetReference(net.getStringId(), net.getIdentifier(), net.getVersion(), net.getTitle().getTranslation(locale), net.getInitials(), net.getTranslatedDefaultCaseName(locale));
        return new PetriNetReference(net, locale);
    }

    static TransitionReference transformToReference(PetriNet net, Transition transition, Locale locale) {
        return new TransitionReference(transition.getStringId(), transition.getTitle().getTranslation(locale), net.getStringId());
    }

    static DataFieldReference transformToReference(PetriNet net, Transition transition, Field field, Locale locale) {
        return new DataFieldReference(field.getStringId(), field.getName().getTranslation(locale), net.getStringId(), transition.getStringId());
    }

    void evictCache();

    PetriNet get(ObjectId petriNetId);

    PetriNet clone(ObjectId petriNetId);
}