package com.netgrif.application.engine.adapter.spring.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.dto.AuthPrincipalDto;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.List;


/**
 * ActionApi provides methods for performing operations and actions on workflow-related entities such as cases,
 * tasks, and users. It facilitates data retrieval, task management, and realm-specific searches within the system.
 */
public interface ActionApi {

    /**
     * Retrieves data associated with a specific task.
     *
     * @param taskId the ID of the task for which data is to be retrieved
     * @param params additional parameters for data retrieval
     * @return the outcome of the data retrieval operation
     */
    GetDataEventOutcome getData(String taskId, Map<String, String> params);

    /**
     * Sets or updates data for a specific task.
     *
     * @param taskId  the ID of the task to use to update data - task does not have to contain the data that is updated
     * @param dataSet the data to be set, organized as a map
     * @param params  additional parameters for the operation
     * @return the outcome of the set data operation
     * @throws JsonProcessingException if there is an error processing JSON data
     */
    SetDataEventOutcome setData(String taskId, Map<String, Map<String, String>> dataSet, Map<String, String> params) throws JsonProcessingException;

    /**
     * Finds a specific case by its ID.
     *
     * @param caseId the ID of the case to find
     * @return the found case
     * @throws IllegalArgumentException if not found
     */
    Case findCase(String caseId);

    /**
     * Searches for cases matching the given predicate.
     *
     * @param processIdentifier reserved for interface compatibility; this implementation
     *                          does not filter by process identifier and returns cases
     *                          from all processes. Use the predicate parameter to filter
     *                          by specific process(es) if needed.
     * @param predicate         the criteria for filtering cases
     * @param pageable          the pagination information
     * @return a page of cases matching the criteria
     */
    Page<Case> searchCases(String processIdentifier, Predicate predicate, Pageable pageable);

    /**
     * Searches for cases using elastic search queries.
     *
     * @param elasticStringQueries a list of queries for filtering cases
     * @param authPrincipalDto     the authorization principal used for the search
     * @param pageable             the pagination information
     * @param isIntersection       true to intersect results of all queries; false for union
     * @return a page of cases matching the criteria
     */
    Page<Case> searchCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection);


    /**
     * Counts the number of cases that match the provided elastic search queries.
     *
     * @param elasticStringQueries a list of elastic search queries used for filtering cases
     * @param authPrincipalDto     the authorization principal for the operation
     * @param isIntersection       true to intersect the results of all queries; false for union
     * @return the total number of cases matching the criteria
     */
    Long countCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Boolean isIntersection);

    /**
     * Creates a new case identified by the given process identifier.
     *
     * @param identifier       the process identifier for creating the case
     * @param title            the title of the new case
     * @param color            the color associated with the case
     * @param authPrincipalDto the authorization principal for creating the case
     * @param params           additional parameters for the operation
     * @return the outcome of the case creation operation
     */
    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, AuthPrincipalDto authPrincipalDto, Map<String, String> params);

    /**
     * Deletes a specific case by its ID.
     *
     * @param caseId the ID of the case to delete
     * @param params additional parameters for the operation
     * @return the outcome of the delete operation
     */
    DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params);

    /**
     * Finds a specific task by its ID.
     *
     * @param taskId the ID of the task to find
     * @return the found task
     * @throws IllegalArgumentException if not found
     */
    Task findTask(String taskId);

    /**
     * Searches for tasks matching the given predicate.
     *
     * @param processIdentifier reserved for interface compatibility; this implementation
     *                          does not filter by process identifier and returns tasks
     *                          from all processes. Use the predicate parameter to filter
     *                          by specific process(es) if needed.
     * @param predicate         the criteria for filtering tasks
     * @param pageable          the pagination information
     * @return a page of tasks matching the criteria
     */
    Page<Task> searchTasks(String processIdentifier, Predicate predicate, Pageable pageable);

    /**
     * Searches for tasks using elastic search queries.
     *
     * @param elasticStringQueries a list of queries for filtering tasks
     * @param authPrincipalDto     the authorization principal used for the search
     * @param pageable             the pagination information
     * @param isIntersection       true to intersect results of all queries; false for union
     * @return a page of tasks matching the criteria
     */
    Page<Task> searchTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection);

    /**
     * Assigns a specific task to a user.
     *
     * @param taskId           the ID of the task to assign
     * @param authPrincipalDto the authorization principal used for the operation
     * @param params           additional parameters for the operation
     * @return the outcome of the task assignment operation
     * @throws TransitionNotExecutableException if the task's transition cannot be executed
     */
    AssignTaskEventOutcome assignTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) throws TransitionNotExecutableException;

    /**
     * Cancels a specific task.
     *
     * @param taskId           the ID of the task to cancel
     * @param authPrincipalDto the authorization principal used for the operation
     * @param params           additional parameters for the operation
     * @return the outcome of the task cancellation operation
     */
    CancelTaskEventOutcome cancelTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params);

    /**
     * Marks a specific task as finished.
     *
     * @param taskId           the ID of the task to finish
     * @param authPrincipalDto the authorization principal used for the operation
     * @param params           additional parameters for the operation
     * @return the outcome of the task completion operation
     * @throws TransitionNotExecutableException if the task's transition cannot be executed
     */
    FinishTaskEventOutcome finishTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) throws TransitionNotExecutableException;

    /**
     * Searches for users in a specific realm based on a predicate.
     *
     * @param predicate the criteria for filtering users
     * @param pageable  the pagination information
     * @param realmId   the ID of the realm to search
     * @return a page of users matching the criteria
     */
    Page<User> searchUsers(Predicate predicate, Pageable pageable, String realmId);


    /**
     * Retrieves the system user of the application.
     *
     * @return the system user
     */
    AbstractUser getSystemUser();
}
