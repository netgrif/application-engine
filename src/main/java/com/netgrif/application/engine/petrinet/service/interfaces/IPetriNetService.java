package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
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

    PetriNet clone(ObjectId petriNetId);

    @Deprecated
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    Optional<PetriNet> save(PetriNet petriNet);

    PetriNet getPetriNet(String id);

    PetriNet getPetriNet(String identifier, Version version);

    List<PetriNet> getByIdentifier(String identifier);

    List<PetriNet> findAllByUri(String uri);

    PetriNet getNewestVersionByIdentifier(String identifier);

    List<PetriNet> getAll();

    FileSystemResource getFile(String netId, String title);

    List<PetriNetReference> getReferences(LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByVersion(Version version, LoggedUser user, Locale locale);

    List<PetriNetReference> getReferencesByUsersProcessRoles(LoggedUser user, Locale locale);

    PetriNetReference getReference(String identifier, Version version, LoggedUser user, Locale locale);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale);

    Page<PetriNetReference> search(Map<String, Object> criteria, LoggedUser user, Pageable pageable, Locale locale);

    Optional<PetriNet> findByImportId(String id);

    static PetriNetReference transformToReference(PetriNet net, Locale locale) {
        //return new PetriNetReference(net.getStringId(), net.getIdentifier(), net.getVersion(), net.getTitle().getTranslation(locale), net.getInitials(), net.getTranslatedDefaultCaseName(locale));
        return new PetriNetReference(net, locale);
    }

    static TransitionReference transformToReference(PetriNet net, Transition transition, Locale locale) {
        List<com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference> list = new ArrayList<>();
        transition.getImmediateData().forEach(fieldId -> list.add(new com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference(net.getDataSet().get(fieldId), locale)));
        return new TransitionReference(transition.getStringId(), transition.getTitle().getTranslation(locale), net.getStringId(), list);
    }

    static DataFieldReference transformToReference(PetriNet net, Transition transition, Field field, Locale locale) {
        return new DataFieldReference(field.getStringId(), field.getName().getTranslation(locale), net.getStringId(), transition.getStringId());
    }

    void evictAllCaches();

    void evictCache(PetriNet net);

    PetriNet get(ObjectId petriNetId);

    List<PetriNet> get(Collection<ObjectId> petriNetId);

    List<PetriNet> get(List<String> petriNetIds);

    void deletePetriNet(String id, LoggedUser loggedUser);

    void runActions(List<Action> actions, PetriNet petriNet);

    List<String> getExistingPetriNetIdentifiersFromIdentifiersList(List<String> identifiers);

    PetriNetImportReference getNetFromCase(String caseId);
}