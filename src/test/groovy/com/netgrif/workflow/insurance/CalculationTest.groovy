package com.netgrif.workflow.insurance

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.workflow.InsurancePostalCodeImporter
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference
import groovy.json.JsonOutput
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CalculationTest {

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private Importer importer

    @Autowired
    private PetriNetRepository repository

    @Autowired
    private AuthorityRepository authorityRepository

    @Autowired
    private IUserService userService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private InsurancePostalCodeImporter postalCodeImporter

    private JsonNodeFactory jsonNodeFactory
    private PetriNet net
    private Case _case
    private Map idConverter

    @Test
    void testCalculation() {
        init()
        "Poistenie nehnuteľnosti a domácnosti"()
        "Základné informácie"()
        "Nehnuteľnosť"()

    }

    private void init() {
        mongoTemplate.getDb().dropDatabase()
        userService.saveNew(new User(
                email: "name.surname@company.com",
                password: "password",
                name: "name",
                surname: "surname",
                authorities: [
                        authorityRepository.save(new Authority("user"))
                ]
        ))
        jsonNodeFactory = JsonNodeFactory.newInstance()
        net = importer.importPetriNet(new File("src/main/resources/petriNets/poistenie_hhi_18_7_2017.xml"), "Household insurance", "HHI")
        _case = workflowService.createCase(net.getStringId(), "Household insurance", "color", 1L)
        idConverter = net.dataSet.collectEntries { [(it.value.importId): (it.key)] }
        postalCodeImporter.run()
    }

    private void "Poistenie nehnuteľnosti a domácnosti"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Poistenie nehnuteľnosti a domácnosti" }.getStringId()

        taskService.assignTask(1L, taskID)
        taskService.finishTask(1L, taskID)
    }

    private void "Základné informácie"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Základné informácie" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
                101001: [
                        value: "81101",
                        type : "text",
                ],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
                301005: [
                        value: "Bratislava",
                        type: "enumeration"
                ],
                101002: [
                        value: false,
                        type: "boolean"
                ],
                101003: [
                        value: false,
                        type: "boolean"
                ],
                101004: [
                        value: false,
                        type: "boolean"
                ],
                101005: [
                        value: "6 až 10",
                        type: "enumeration"
                ],
                101006: [
                        value: "vlastník nehnuteľnosti",
                        type: "enumeration"
                ],
                101007: [
                        value: "2",
                        type: "enumeration"
                ],
                101008: [
                        value: "1",
                        type: "enumeration"
                ],
                101009: [
                        value: false,
                        type: "boolean"
                ],
                101010: [
                        value: false,
                        type: "boolean"
                ],
                101011: [
                        value: false,
                        type: "boolean"
                ],
                101012: [
                        value: false,
                        type: "boolean"
                ],
                101013: [
                        value: "počas celého dňa",
                        type: "boolean"
                ],
                101014: [
                        value: false,
                        type: "boolean"
                ],
                101015: [
                        value: "fyzická osoba",
                        type: "enumeration"
                ],
                101016: [
                        value: "0",
                        type: "enumeration"
                ],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private void "Nehnuteľnosť"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Nehnuteľnosť" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
                102001: [
                        value: "byt",
                        type : "enumeration",
                ],
                105005: [
                        value: "50.00 €",
                        type : "enumeration",
                ],
                105001: [
                        value: 10,
                        type : "number",
                ],
                105002: [
                        value: 20,
                        type : "number",
                ],
                105003: [
                        value: "30",
                        type : "number",
                ],
                102002: [
                        value: "tehla a/alebo betón",
                        type : "enumeration",
                ],
                102003: [
                        value: "škridla",
                        type : "enumeration",
                ],
                102004: [
                        value: "6 až 10",
                        type : "enumeration",
                ],
                102005: [
                        value: false,
                        type : "boolean",
                ],
                102006: [
                        value: "1",
                        type : "enumeration",
                ],
                102007: [
                        value: "1",
                        type : "enumeration",
                ],
                102008: [
                        value: "bez rekonštrukcie",
                        type : "enumeration",
                ],

                104001: [
                        value: true,
                        type : "boolean",
                ],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
                107001: [
                        value: "15,000.00 €",
                        type : "enumeration",
                ],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private ObjectNode populateDataset(Map<Long, Map<String, String>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = JsonOutput.toJson(data.collectEntries {[(idConverter.get(it.key as Long)): it.value]})
        return mapper.readTree(json) as ObjectNode
    }
}