package com.netgrif.application.engine.migration

import com.netgrif.application.engine.elastic.service.ElasticCaseService
import com.netgrif.application.engine.elastic.service.ElasticTaskService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import java.util.stream.Stream

@Slf4j
@Component
@ConditionalOnProperty(value = "netgrif.engine.migration.process-resource-id-migration.enabled", havingValue = "true")
class ProcessResourceIdMigration extends MigrationOrderedCommandLineRunner {

    MigrationHelper migrationHelper

    MongoTemplate mongoTemplate

    Map<String, String> processIdIdentifierMap = new HashMap<>()

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

                migratePetriNet(useCase)

                migrateTasksOfCase(useCase, oldCaseId, newCaseId)

                migrateCasePermissions(useCase)

                migrateDataFields(useCase)

                useCase.set_id(newCaseId)

                mongoTemplate.insert(useCase)
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldCaseId)), Case.class)
                elasticCaseService.index(elasticCaseMappingService.transform(useCase))
                elasticCaseService.remove(oldCaseId.toString())
            }
        }
    }

    private String getNewIdFromOldId(String oldId) {
        String[] parts = oldId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid composite ID format: " + oldId);
        }
        String processId = ProcessResourceId.decodeShortProcessId(parts[0])
        if (processIdIdentifierMap.containsKey(processId)) {
           return new ProcessResourceId(processIdIdentifierMap.get(processId), parts[1]).toString()
        } else {
            try {
                PetriNet petriNet = mongoTemplate.findById(new ObjectId(processId), PetriNet.class)
                if (petriNet != null) {
                    processIdIdentifierMap.put(processId, petriNet.getIdentifier())
                    return new ProcessResourceId(petriNet.getIdentifier(), parts[1]).toString()
                }
            } catch (IllegalArgumentException e) {
                log.error("Error while update reference fields", e)
            }
        }
    }

    private void migratePetriNet(Case useCase) {
        if (!processIdIdentifierMap.containsKey(useCase.getPetriNetId())) {
            PetriNet petriNet = mongoTemplate.findById(useCase.getPetriNetObjectId(), PetriNet.class)
            processIdIdentifierMap.put(petriNet.getStringId(), petriNet.getIdentifier())

            if (petriNet.getRoles() != null && !petriNet.getRoles().isEmpty()) {
                migrateProcessRoles(petriNet)
            }

            if (petriNet.getPermissions() != null && !petriNet.getPermissions().isEmpty()) {
                petriNet.setPermissions(migratePetriNetPermissions(petriNet.getPermissions()))
            }
            if (petriNet.getNegativeViewRoles() != null && !petriNet.getNegativeViewRoles().isEmpty()) {
                petriNet.setNegativeViewRoles(migrateRoleIds(petriNet.getNegativeViewRoles()))
            }
            mongoTemplate.save(petriNet)
        }
    }

    private void migrateTasksOfCase(Case useCase, ProcessResourceId oldCaseId, ProcessResourceId newCaseId) {
        Set<TaskPair> newTaskPairs = new HashSet<>()
        List<Task> oldTasks = mongoTemplate.find(Query.query(Criteria.where("caseId").is(oldCaseId.shortProcessId + ProcessResourceId.ID_SEPARATOR + oldCaseId.getObjectId().toString())), Task.class)
        oldTasks.forEach { task ->
            migrateTask(useCase, newCaseId, newTaskPairs, task)
        }
        useCase.setTasks(newTaskPairs)
    }

    private void migrateTask(Case useCase, ProcessResourceId newCaseId, Set<TaskPair> newTaskPairs, Task task) {
        ProcessResourceId oldTaskId = task.get_id()
        oldTaskId.setShortProcessIdentifier(null)
        ProcessResourceId newTaskId = new ProcessResourceId(useCase.getProcessIdentifier(), oldTaskId.getObjectId())
        if (newTaskId != oldTaskId) {
            task.set_id(newTaskId)
            task.setProcessIdentifier(useCase.getProcessIdentifier())
            task.setCaseId(newCaseId.toString())
        }

        if (task.getRoles() != null && !task.getRoles().isEmpty()) {
            Map<String, Map<String, Boolean>> newValues = new HashMap<>()
            task.getRoles().each { oldRoleId, permissions ->
                if (!oldRoleId.startsWith(ProcessResourceId.NONE_SHORT_ID_VALUE)) {
                    newValues.put(getNewIdFromOldId(oldRoleId), permissions)
                } else {
                    newValues.put(oldRoleId, permissions)
                }
            }
            task.setRoles(newValues)
        }

        if (task.getViewRoles() != null && !task.getViewRoles().isEmpty()) {
            List<String> newValues = new ArrayList<>()
            task.getViewRoles().each { oldRoleId ->
                if (!oldRoleId.startsWith(ProcessResourceId.NONE_SHORT_ID_VALUE)) {
                    newValues.add(getNewIdFromOldId(oldRoleId))
                } else {
                    newValues.add(oldRoleId)
                }
            }
            task.setViewRoles(newValues)
        }

        if (task.getNegativeViewRoles() != null && !task.getNegativeViewRoles().isEmpty()) {
            List<String> newValues = new ArrayList<>()
            task.getNegativeViewRoles().each { oldRoleId ->
                if (!oldRoleId.startsWith(ProcessResourceId.NONE_SHORT_ID_VALUE)) {
                    newValues.add(getNewIdFromOldId(oldRoleId))
                } else {
                    newValues.add(oldRoleId)
                }
            }
            task.setNegativeViewRoles(newValues)
        }

        mongoTemplate.insert(task)
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldTaskId)), Task.class)

        elasticTaskService.index(elasticTaskMappingService.transform(task))
        elasticTaskService.remove(oldTaskId.getStringId())
        newTaskPairs.add(new TaskPair(newTaskId.toString(), task.transitionId))
    }

    private void migrateProcessRoles(PetriNet petriNet) {
        Map<String, ProcessRole> newValues = new LinkedHashMap<>()
        petriNet.getRoles().each { oldRoleStringId, processRole ->
            if (!processRole.isGlobal()) {
                ProcessResourceId oldRoleId = processRole.get_id()
                oldRoleId.setShortProcessIdentifier(null)
                ProcessResourceId newRoleId = new ProcessResourceId(processRole.getProcessIdentifier(), oldRoleId.getObjectId())
                processRole.set_id(newRoleId)

                mongoTemplate.insert(processRole)
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(oldRoleId)), ProcessRole.class)

                newValues.put(newRoleId.toString(), processRole)
            } else {
                newValues.put(oldRoleStringId, processRole)
            }
        }
        petriNet.setRoles(newValues)
    }

    private void migrateCasePermissions(Case useCase) {
        if (useCase.getEnabledRoles() != null && !useCase.getEnabledRoles().isEmpty()) {
            useCase.setEnabledRoles(new HashSet<>(migrateRoleIds(useCase.getEnabledRoles())))
        }
        if (useCase.getViewRoles() != null && !useCase.getViewRoles().isEmpty()) {
            useCase.setViewRoles(migrateRoleIds(useCase.getViewRoles()))
        }

        if (useCase.getNegativeViewRoles() != null && !useCase.getNegativeViewRoles().isEmpty()) {
            useCase.setNegativeViewRoles(migrateRoleIds(useCase.getNegativeViewRoles()))
        }

        if (useCase.getPermissions() != null && !useCase.getPermissions().isEmpty()) {
            useCase.setPermissions(migratePetriNetPermissions(useCase.getPermissions()))
        }
    }

    private void migrateDataFields(Case useCase) {
        useCase.dataSet.each { key, dataField ->
            Field<?> field = useCase.getField(key)
            if (field instanceof FieldWithAllowedNets) {
                List<String> oldValues = (List<String>) dataField.getValue()
                List<String> newValues = new ArrayList<>()
                if (oldValues != null) {
                    oldValues.forEach { oldStringId ->
                        newValues.add(getNewIdFromOldId(oldStringId))
                    }
                }
                dataField.setValue(newValues)
            }
        }
    }

    private Map<String, Map<String, Boolean>> migratePetriNetPermissions(Map<String, Map<String, Boolean>> rolePermissionMap) {
        Map<String, Map<String, Boolean>> newValues = new HashMap<>()
        rolePermissionMap.each { oldRoleId, permissions ->
            if (!oldRoleId.startsWith(ProcessResourceId.NONE_SHORT_ID_VALUE)) {
                newValues.put(getNewIdFromOldId(oldRoleId), permissions)
            } else {
                newValues.put(oldRoleId, permissions)
            }
        }
        return newValues
    }

    private List<String> migrateRoleIds(Collection<String> oldRoleIds) {
        List<String> newValues = new ArrayList<>()
        oldRoleIds.each { oldRoleId ->
            if (!oldRoleId.startsWith(ProcessResourceId.NONE_SHORT_ID_VALUE)) {
                newValues.add(getNewIdFromOldId(oldRoleId))
            } else {
                newValues.add(oldRoleId)
            }
        }
        return newValues;
    }
}
