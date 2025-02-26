package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetImportReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.TransitionReference;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import org.bson.types.ObjectId;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface IPetriNetService {

    //TODO release/8.0.0 resolve
    static PetriNetReference transformToReference(Process net, Locale locale) {
        //return new PetriNetReference(net.getStringId(), net.getIdentifier(), net.getVersion(), net.getTitle().getTranslation(locale), net.getInitials(), net.getTranslatedDefaultCaseName(locale));
        return new PetriNetReference(net, locale);
    }

    static TransitionReference transformToReference(Process net, Transition transition, Locale locale) {
        List<com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference> list = new ArrayList<>();
        transition.getImmediateData().forEach(fieldId -> list.add(new com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference(net.getDataSet().get(fieldId), locale)));
        return new TransitionReference(transition.getStringId(), transition.getTitle().getTranslation(locale), net.getStringId(), list);
    }

    static DataFieldReference transformToReference(Process net, Transition transition, Field field, Locale locale) {
        return new DataFieldReference(field.getStringId(), field.getTitle().getTranslation(locale), net.getStringId(), transition.getStringId());
    }

    Process clone(ObjectId petriNetId);

    @Deprecated
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    @Deprecated
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser user, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user, String uriNodeId, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    Optional<Process> save(Process petriNet);

    Process getPetriNet(String id);

    Process getPetriNet(String identifier, Version version);

    List<Process> getByIdentifier(String identifier);

    List<Process> findAllByUriNodeId(String uriNodeId);

    List<Process> findAllById(List<String> ids);

    Process getNewestVersionByIdentifier(String identifier);

    List<Process> getAll();

    FileSystemResource getFile(String netId, String title);

    List<PetriNetReference> getReferences(LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByVersion(Version version, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByUsersRoles(LoggedUser user, Locale locale);

    PetriNetReference getReference(String identifier, Version version, LoggedUser user, Locale locale);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale);

    Page<PetriNetReference> search(PetriNetSearch criteria, LoggedUser user, Pageable pageable, Locale locale);

    Optional<Process> findByImportId(String id);

    void evictAllCaches();

    void evictCache(Process net);

    Process get(ObjectId petriNetId);

    List<Process> get(Collection<ObjectId> petriNetId);

    List<Process> get(List<String> petriNetIds);

    void deletePetriNet(String id);

    void runActions(List<Action> actions, Process petriNet);

    List<String> getExistingPetriNetIdentifiersFromIdentifiersList(List<String> identifiers);

    PetriNetImportReference getNetFromCase(String caseId);
}