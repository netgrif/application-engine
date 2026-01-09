package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.*;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingIconKeyException;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetImportReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.TransitionReference;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import org.bson.types.ObjectId;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Interface defining methods for managing {@link PetriNet} objects.
 */
public interface IPetriNetService {

    /** 
     * Transforms a {@link PetriNet} into a {@link PetriNetReference}.
     * 
     * @param net the PetriNet to transform
     * @param locale the locale to use for translations
     * @return a {@link PetriNetReference} representing the given PetriNet
     */
    static PetriNetReference transformToReference(PetriNet net, Locale locale) {
        return new PetriNetReference(net, locale);
    }

    /** 
     * Transforms a {@link Transition} into a {@link TransitionReference}.
     * 
     * @param net the {@link PetriNet} containing the transition
     * @param transition the transition to transform
     * @param locale the locale to use for translations
     * @return a {@link TransitionReference} for the given transition
     */
    static TransitionReference transformToReference(PetriNet net, Transition transition, Locale locale) {
        List<com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference> list = new ArrayList<>();
        transition.getImmediateData().forEach(fieldId -> list.add(new com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference(net.getDataSet().get(fieldId), locale)));
        return new TransitionReference(transition.getStringId(), transition.getTitle().getTranslation(locale), net.getStringId(), list);
    }

    /** 
     * Transforms a {@link Field} into a {@link DataFieldReference}.
     * 
     * @param net the {@link PetriNet} containing the field
     * @param transition the {@link Transition} containing the field
     * @param field the field to transform
     * @param locale the locale to use for translations
     * @return a {@link DataFieldReference} for the given field
     */
    static DataFieldReference transformToReference(PetriNet net, Transition transition, Field field, Locale locale) {
        return new DataFieldReference(field.getStringId(), field.getName().getTranslation(locale), net.getStringId(), transition.getStringId());
    }

    /**
     * Imports a PetriNet from XML input. 
     * 
     * @param xmlFile the input stream of the XML file
     * @param releaseType the type of release
     * @param user the user performing the import
     * @return an {@link ImportPetriNetEventOutcome} representing the result
     * @throws IOException if an I/O error occurs
     * @throws MissingPetriNetMetaDataException if metadata is incomplete
     * @throws MissingIconKeyException if an icon key is missing
     * @deprecated Use {@link #importPetriNet(InputStream, VersionType, LoggedUser)} instead.
     */
    @Deprecated
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    /**
     * Imports a PetriNet from XML input.
     *
     * @param xmlFile the input stream of the XML file
     * @param releaseType the type of release {@link VersionType}
     * @param user the user performing the import
     * @return an {@link ImportPetriNetEventOutcome} representing the result
     * @throws IOException if an I/O error occurs
     * @throws MissingPetriNetMetaDataException if metadata is incomplete
     * @throws MissingIconKeyException if an icon key is missing
     */
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;


    /**
     * Imports a PetriNet from XML input.
     *
     * @param xmlFile the input stream of the XML file
     * @param releaseType the type of release {@link VersionType}
     * @param user the user performing the import
     * @param params additional parameters
     * @return an {@link ImportPetriNetEventOutcome} representing the result
     * @throws IOException if an I/O error occurs
     * @throws MissingPetriNetMetaDataException if metadata is incomplete
     * @throws MissingIconKeyException if an icon key is missing
     */
    ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser user, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException;

    /**
     * Saves a PetriNet object.
     *
     * @param petriNet the PetriNet to save
     * @return an {@link Optional} containing the saved PetriNet or empty if unsuccessful
     */
    Optional<PetriNet> save(PetriNet petriNet);

    /**
     * Retrieves a {@link PetriNet} by its ID.
     *
     * @param id the ID of the PetriNet
     * @return the corresponding {@link PetriNet}
     */
    PetriNet getPetriNet(String id);

    /**
     * Retrieves a {@link PetriNet} by its identifier and version.
     *
     * @param identifier the unique identifier of the PetriNet
     * @param version the version of the PetriNet
     * @return the {@link PetriNet} matching the provided identifier and version
     */
    PetriNet getPetriNet(String identifier, Version version);

    /**
     * Retrieves a paginated list of {@link PetriNet} objects by their identifier.
     *
     * @param identifier the unique identifier of the PetriNets
     * @param pageable the pagination information
     * @return a paginated list of {@link PetriNet} objects
     */
    Page<PetriNet> getByIdentifier(String identifier, Pageable pageable);

    /**
     * Finds all {@link PetriNet} objects by their IDs.
     *
     * @param ids a list of PetriNet IDs to retrieve
     * @return a list of {@link PetriNet} objects matching the provided IDs
     */
    List<PetriNet> findAllById(List<String> ids);

    /**
     * Retrieves the default version of a {@link PetriNet} by its identifier.
     *
     * @param identifier the unique identifier of the PetriNet
     * @return the default version of the {@link PetriNet} matching the provided identifier or null if not found
     */
    PetriNet getDefaultVersionByIdentifier(String identifier);

    /**
     * Retrieves the latest version of a {@link PetriNet} by its identifier.
     *
     * @param identifier the unique identifier of the PetriNet
     * @return the latest version of the {@link PetriNet} matching the provided identifier or null if not found
     */
    PetriNet getLatestVersionByIdentifier(String identifier);

    /**
     * Retrieves a paginated list of all {@link PetriNet} objects.
     *
     * @param pageable the pagination information
     * @return a paginated list of all {@link PetriNet} objects
     */
    Page<PetriNet> getAll(Pageable pageable);

    /**
     * Retrieves a {@link FileSystemResource} representing a file associated with a {@link PetriNet}.
     *
     * @param netId the unique ID of the PetriNet
     * @param title the title of the file to retrieve
     * @return a {@link FileSystemResource} containing the file
     */
    FileSystemResource getFile(String netId, String title);

    /**
     * Retrieves a paginated list of PetriNet references accessible by the given user.
     *
     * @param user the logged-in user
     * @param locale the locale for translations
     * @param pageable the pagination information
     * @return a {@link Page} of {@link PetriNetReference} objects
     */ 
    Page<PetriNetReference> getReferences(LoggedUser user, Locale locale, Pageable pageable);

    /**
     * Retrieves a paginated list of {@link PetriNetReference} objects by their identifier.
     *
     * @param identifier the unique identifier of the PetriNet
     * @param user the logged-in user making the request
     * @param locale the locale for translations
     * @param pageable the pagination information
     * @return a paginated list of {@link PetriNetReference} objects
     */
    Page<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale, Pageable pageable);

    /**
     * Retrieves a paginated list of {@link PetriNetReference} objects by version.
     *
     * @param version the {@link Version} of the PetriNet
     * @param user the logged-in user making the request
     * @param locale the locale for translations
     * @param pageable the pagination information
     * @return a paginated list of {@link PetriNetReference} objects
     */
    Page<PetriNetReference> getReferencesByVersion(Version version, LoggedUser user, Locale locale, Pageable pageable);

    /**
     * Retrieves a list of {@link PetriNetReference} objects accessible by the user's process roles.
     *
     * @param user the logged-in user making the request
     * @param locale the locale for translations
     * @return a list of {@link PetriNetReference} objects accessible by the user
     */
    List<PetriNetReference> getReferencesByUsersProcessRoles(LoggedUser user, Locale locale);

    /**
     * Retrieves a single {@link PetriNetReference} by identifier and version.
     *
     * @param identifier the unique identifier of the PetriNet
     * @param version the {@link Version} of the PetriNet
     * @param user the logged-in user making the request
     * @param locale the locale for translations
     * @return the {@link PetriNetReference} object corresponding to the given identifier and version
     */
    PetriNetReference getReference(String identifier, Version version, LoggedUser user, Locale locale);

    /**
     * Retrieves a list of {@link TransitionReference} objects for the given PetriNet IDs.
     *
     * @param netsIds the list of PetriNet IDs for which to retrieve transitions
     * @param user the logged-in user making the request
     * @param locale the locale for translations
     * @return a list of {@link TransitionReference} objects for the given PetriNet IDs
     */
    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    /**
     * Retrieves a list of {@link DataFieldReference} objects for the given transition references.
     *
     * @param transitions the list of {@link TransitionReference} objects
     * @param locale the locale for translations
     * @return a list of {@link DataFieldReference} objects
     */
    List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale);

    /**
     * Performs a search for {@link PetriNetReference} objects based on the provided criteria.
     *
     * @param criteria the {@link PetriNetSearch} criteria to filter results
     * @param user the logged-in user making the request
     * @param pageable the pagination information
     * @param locale the locale for translations
     * @return a paginated list of {@link PetriNetReference} objects matching the criteria
     */
    Page<PetriNetReference> search(PetriNetSearch criteria, LoggedUser user, Pageable pageable, Locale locale);

    /**
     * Finds a {@link PetriNet} by its import ID.
     *
     * @param id the import ID of the PetriNet
     * @return an {@link Optional} containing the {@link PetriNet} if found, otherwise empty
     */
    Optional<PetriNet> findByImportId(String id);

    /**
     * Evicts all cached PetriNet data from caches.
     */
    void evictAllCaches();

    /**
     * Evicts the cache for the given {@link PetriNet}.
     *
     * @param net the {@link PetriNet} to remove from the cache
     */
    void evictCache(PetriNet net);

    /**
     * Retrieves a {@link PetriNet} by its MongoDB {@link ObjectId}.
     *
     * @param petriNetId the {@link ObjectId} of the PetriNet to retrieve
     * @return the {@link PetriNet} associated with the given ID
     */
    PetriNet get(ObjectId petriNetId);

    /**
     * Retrieves a list of {@link PetriNet} objects by their MongoDB {@link ObjectId}s.
     *
     * @param petriNetId a collection of {@link ObjectId}s of the PetriNets to retrieve
     * @return a list of {@link PetriNet} objects corresponding to the provided IDs
     */
    List<PetriNet> get(Collection<ObjectId> petriNetId);

    /**
     * Retrieves a list of {@link PetriNet} objects by their unique string identifiers.
     *
     * @param petriNetIds a list of unique identifiers of PetriNets to retrieve
     * @return a list of {@link PetriNet} objects corresponding to the provided IDs
     */
    List<PetriNet> get(List<String> petriNetIds);

    /**
     * Deletes a PetriNet by its ID.
     *
     * @param id the ID of the PetriNet to delete
     * @param loggedUser the user requesting the deletion
     */
    void deletePetriNet(String id, LoggedUser loggedUser);


    /**
     * Forcefully deletes a PetriNet process by its ID.
     *
     * @param processId  the ID of the process to delete
     * @param loggedUser the user requesting the deletion
     */
    void forceDeletePetriNet(String processId, LoggedUser loggedUser);

    /**
     * Runs the specified set of actions on a PetriNet.
     *
     * @param actions the actions to execute
     * @param petriNet the PetriNet on which actions are executed
     */
    void runActions(List<Action> actions, PetriNet petriNet);

    /**
     * Retrieves a list of existing PetriNet identifiers from the given list of identifiers.
     * Each provided identifier is validated, and only identifiers corresponding to existing PetriNets are returned.
     *
     * @param identifiers the list of PetriNet identifiers to validate
     * @return a list of validated and existing PetriNet identifiers
     */
    List<String> getExistingPetriNetIdentifiersFromIdentifiersList(List<String> identifiers);

    /**
     * Retrieves the reference of a {@link PetriNet} associated with a case ID.
     *
     * @param caseId the ID of the workflow case
     * @return a {@link PetriNetImportReference} linking the PetriNet
     */
    PetriNetImportReference getNetFromCase(String caseId);


    /**
     * Retrieves a paginated list of {@link PetriNet} objects associated with a specific role ID.
     *
     * @param roleId   the ID of the role to filter the PetriNets by
     * @param pageable the pagination information
     * @return a {@link Page} of {@link PetriNet} objects matching the role ID
     */
    /**
     * Retrieves a paginated list of {@link PetriNet} objects associated with a specific role ID.
     *
     * @param roleId   the ID of the role to filter the PetriNets by
     * @param pageable the pagination information
     * @return a {@link Page} of {@link PetriNet} objects matching the role ID
     */
    Page<PetriNet> findAllByRoleId(String roleId, Pageable pageable);
}