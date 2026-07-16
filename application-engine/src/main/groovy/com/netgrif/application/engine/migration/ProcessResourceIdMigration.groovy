package com.netgrif.application.engine.migration

import com.netgrif.application.engine.elastic.service.ElasticCaseService
import com.netgrif.application.engine.elastic.service.ElasticIndexService
import com.netgrif.application.engine.elastic.service.ElasticTaskMappingService
import com.netgrif.application.engine.elastic.service.ElasticTaskService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService
import com.netgrif.application.engine.objects.elastic.domain.TaskField
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.dataset.CaseField
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldWithAllowedNets
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.TaskPair
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import java.util.stream.Stream

@Slf4j
@Component
class ProcessResourceIdMigration extends MigrationOrderedCommandLineRunner {

    MigrationHelper migrationHelper

    MongoTemplate mongoTemplate

    Map<String, String> processIdentifierIdMap = new HashMap<>()

    ElasticCaseService elasticCaseService

    ElasticTaskService elasticTaskService

    IElasticCaseMappingService elasticCaseMappingService

    IElasticTaskMappingService elasticTaskMappingService

    ProcessResourceIdMigration(MigrationHelper migrationHelper,
                               @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
                               ElasticCaseService elasticCaseService,
                               ElasticTaskService elasticTaskService,
                               IElasticCaseMappingService elasticCaseMappingService,
                               IElasticTaskMappingService elasticTaskMappingService) {
        this.migrationHelper = migrationHelper
        this.mongoTemplate = mongoTemplate
        this.elasticCaseService = elasticCaseService
        this.elasticTaskService = elasticTaskService
        this.elasticCaseMappingService = elasticCaseMappingService
        this.elasticTaskMappingService = elasticTaskMappingService
    }

    @Override
    void migrate() {
        Query roleQuery = Query.query(Criteria.where("_id.shortProcessId").exists(true))
        roleQuery.cursorBatchSize(500)
        try (Stream<ProcessRole> cursorStream = mongoTemplate.stream(roleQuery, ProcessRole.class)) {
            Iterator<ProcessRole> cursor = cursorStream.iterator()
            while (cursor.hasNext()) {
                ProcessRole processRole = cursor.next()

                if (processRole.isGlobal()) {
                    continue
                }

                ProcessResourceId oldRoleId = processRole.get_id();
                oldRoleId.setShortProcessIdentifier(null)
                ProcessResourceId newRoleId = new ProcessResourceId(processRole.getProcessIdentifier(), oldRoleId.getObjectId())
                processRole.set_id(newRoleId)

                mongoTemplate.insert(processRole)
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldRoleId)), ProcessRole.class)
            }
        }

        Query caseQuery = Query.query(Criteria.where("_id.shortProcessId").exists(true))
        caseQuery.cursorBatchSize(500)
        try (Stream<Case> cursorStream = mongoTemplate.stream(caseQuery, Case.class)) {
            Iterator<Case> cursor = cursorStream.iterator()

            while (cursor.hasNext()) {
                Case useCase = cursor.next()

                ProcessResourceId oldCaseId = useCase.get_id()
                oldCaseId.setShortProcessIdentifier(null)
                ProcessResourceId newCaseId = new ProcessResourceId(useCase.getProcessIdentifier(), oldCaseId.getObjectId())

                if (newCaseId == oldCaseId) {
                    continue
                }

                if (useCase.getTasks() != null && !useCase.getTasks().isEmpty()) {
                    Set<TaskPair> newTaskPairs = new HashSet<>()
                    List<Task> oldTasks = mongoTemplate.find(Query.query(Criteria.where("caseId").is(oldCaseId.shortProcessId + ProcessResourceId.ID_SEPARATOR + oldCaseId.getObjectId().toString())), Task.class)
                    oldTasks.forEach {
                        ProcessResourceId oldTaskId = it.get_id()
                        oldTaskId.setShortProcessIdentifier(null)
                        ProcessResourceId newTaskId = new ProcessResourceId(useCase.getProcessIdentifier(), oldTaskId.getObjectId())
                        if (newTaskId != oldTaskId) {
                            it.set_id(newTaskId)
                            it.setProcessIdentifier(useCase.getProcessIdentifier())
                            it.setCaseId(newCaseId.toString())
                        }
                        mongoTemplate.insert(it)
                        mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldTaskId)), Task.class)

                        elasticTaskService.index(elasticTaskMappingService.transform(it))
                        elasticTaskService.remove(oldTaskId.getStringId())
                        newTaskPairs.add(new TaskPair(newTaskId.toString(), it.transitionId))
                    }
                    useCase.setTasks(newTaskPairs)
                }

                if (useCase.getEnabledRoles() != null && !useCase.getEnabledRoles().isEmpty()) {
                    Set<String> newValues = new HashSet<>()
                    useCase.getEnabledRoles().each { oldRoleId ->
                        String[] parts = oldRoleId.split(ProcessResourceId.ID_SEPARATOR);
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Invalid composite ID format: " + oldRoleId);
                        }
                        String processId = ProcessResourceId.decodeShortProcessId(parts[0])
                        if (processIdentifierIdMap.containsKey(processId)) {
                            newValues.add(new ProcessResourceId(processIdentifierIdMap.get(processId), parts[1]).toString())
                        } else {
                            try {
                                PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                                if (petriNet != null) {
                                    processIdentifierIdMap.put(processId, petriNet.getIdentifier())
                                    newValues.add(new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString())
                                }
                            } catch (IllegalArgumentException e) {
                                log.error("Error while update reference fields", e)
                            }
                        }
                    }
                    useCase.setEnabledRoles(newValues)
                }
                if (useCase.getViewRoles() != null && !useCase.getViewRoles().isEmpty()) {
                    List<String> newValues = new ArrayList<>()
                    useCase.getViewRoles().each { oldRoleId ->
                        String[] parts = oldRoleId.split(ProcessResourceId.ID_SEPARATOR);
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Invalid composite ID format: " + oldRoleId);
                        }
                        String processId = ProcessResourceId.decodeShortProcessId(parts[0])
                        if (processIdentifierIdMap.containsKey(processId)) {
                            newValues.add(new ProcessResourceId(processIdentifierIdMap.get(processId), parts[1]).toString())
                        } else {
                            try {
                                PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                                if (petriNet != null) {
                                    processIdentifierIdMap.put(processId, petriNet.getIdentifier())
                                    newValues.add(new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString())
                                }
                            } catch (IllegalArgumentException e) {
                                log.error("Error while update reference fields", e)
                            }
                        }
                    }
                    useCase.setViewRoles(newValues)
                }

                if (useCase.getNegativeViewRoles() != null && !useCase.getNegativeViewRoles().isEmpty()) {
                    List<String> newValues = new ArrayList<>()
                    useCase.getNegativeViewRoles().each { oldRoleId ->
                        String[] parts = oldRoleId.split(ProcessResourceId.ID_SEPARATOR);
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Invalid composite ID format: " + oldRoleId);
                        }
                        String processId = ProcessResourceId.decodeShortProcessId(parts[0])
                        if (processIdentifierIdMap.containsKey(processId)) {
                            newValues.add(new ProcessResourceId(processIdentifierIdMap.get(processId), parts[1]).toString())
                        } else {
                            try {
                                PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                                if (petriNet != null) {
                                    processIdentifierIdMap.put(processId, petriNet.getIdentifier())
                                    newValues.add(new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString())
                                }
                            } catch (IllegalArgumentException e) {
                                log.error("Error while update reference fields", e)
                            }
                        }
                    }
                    useCase.setNegativeViewRoles(newValues)
                }

                if (useCase.getPermissions() != null && !useCase.getPermissions().isEmpty()) {
                    Map<String, Map<String, Boolean>> newValues = new HashMap<>()
                    useCase.getPermissions().each { oldRoleId, permissions ->
                        String[] parts = oldRoleId.split(ProcessResourceId.ID_SEPARATOR);
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Invalid composite ID format: " + oldRoleId);
                        }
                        String processId = ProcessResourceId.decodeShortProcessId(parts[0])
                        if (processIdentifierIdMap.containsKey(processId)) {
                            newValues.put(new ProcessResourceId(processIdentifierIdMap.get(processId), parts[1]).toString(), permissions)
                        } else {
                            try {
                                PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                                if (petriNet != null) {
                                    processIdentifierIdMap.put(processId, petriNet.getIdentifier())
                                    newValues.put(new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString(), permissions)
                                }
                            } catch (IllegalArgumentException e) {
                                log.error("Error while update reference fields", e)
                            }
                        }
                    }
                    useCase.setPermissions(newValues)
                }

                useCase.dataSet.each { key, dataField ->
                    Field<?> field = useCase.getField(key)
                    if (field instanceof FieldWithAllowedNets) {
                        List<String> oldValues = (List<String>) dataField.getValue()
                        List<String> newValues = new ArrayList<>()
                        if (oldValues != null) {
                            oldValues.forEach { oldStringId ->
                                String[] parts = oldStringId.split(ProcessResourceId.ID_SEPARATOR);
                                if (parts.length != 2) {
                                    throw new IllegalArgumentException("Invalid composite ID format: " + oldStringId);
                                }
                                String processId = ProcessResourceId.decodeShortProcessId(parts[0])
                                if (processIdentifierIdMap.containsKey(processId)) {
                                    newValues.add(new ProcessResourceId(processIdentifierIdMap.get(processId), parts[1]).toString())
                                } else {
                                    try {
                                        PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                                        if (petriNet != null) {
                                            processIdentifierIdMap.put(processId, petriNet.getIdentifier())
                                            newValues.add(new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString())
                                        }
                                    } catch (IllegalArgumentException e) {
                                        log.error("Error while update reference fields", e)
                                    }
                                }
                            }
                        }
                        dataField.setValue(newValues)
                    }
                }

                useCase.set_id(newCaseId)

                mongoTemplate.insert(useCase)
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldCaseId)), Case.class)
                elasticCaseService.index(elasticCaseMappingService.transform(useCase))
                elasticCaseService.remove(oldCaseId.toString())
            }
        }
    }
}
