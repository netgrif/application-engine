package com.netgrif.workflow.insurance.calculation

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
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference
import groovy.json.JsonOutput
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CalculationTest3 {

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private JdbcTemplate jdbcTemplate

    @Autowired
    private Importer importer

    @Autowired
    private PetriNetRepository repository

    @Autowired
    private AuthorityRepository authorityRepository

    @Autowired
    private CaseRepository caseRepository

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
        "Nehnuteľnosť a domácnosť"()
        "Základné informácie"()
        "Nehnuteľnosť"()
        "Doplnkové poistenie nehnuteľnosti"()
        "Vedľajšie stavby"()
        "Sumár"()
        assertCalculation()
    }

    private void init() {
        mongoTemplate.getDb().dropDatabase()
        jdbcTemplate.update("TRUNCATE TABLE postal_code")
        userService.saveNew(new User(
                email: "name3.surname@company.com",
                password: "password3",
                name: "name3",
                surname: "surname3",
                authorities: [
                        authorityRepository.findByName("user")?:authorityRepository.save(new Authority("user"))
                ]
        ))
        jsonNodeFactory = JsonNodeFactory.newInstance()
        net = importer.importPetriNet(new File("src/main/resources/petriNets/poistenie_hhi_18_7_2017.xml"), "Household insurance", "HHI")
        _case = workflowService.createCase(net.getStringId(), "Household insurance3", "color", 1L)
        idConverter = net.dataSet.collectEntries { [(it.value.importId): (it.key)] }
        postalCodeImporter.run()
    }

    private void "Nehnuteľnosť a domácnosť"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Iba nehnuteľnosť" }.getStringId()

        taskService.assignTask(1L, taskID)
        taskService.finishTask(1L, taskID)
    }

    private void "Základné informácie"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Základné informácie" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                PSC
101001: [
        value: "84104",
        type : "text",
],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Mesto
301005: [
        value: "Bratislava",
        type : "enumeration"
],
//                Nachádza sa mimo obce (extravilán)?
101002: [
        value: false,
        type : "boolean"
],
//                Bolo miesto poistenia postihnuté povodňou za posledných 10 rokov?
101003: [
        value: false,
        type : "boolean"
],
//                Nachádza nehnuteľnosť sa vo vzdialenosti kratšej ako 300 m od vodného toku?
101004: [
        value: false,
        type : "boolean"
],
//                Koľko rokov žijete v poisťovanej nehnuteľnosti?
101005: [
        value: "6 až 10",
        type : "enumeration"
],
//                Aký je vzťah poisteného k poisťovanej nehnuteľnosti?
101006: [
        value: "vlastník nehnuteľnosti",
        type : "enumeration"
],
//                Koľko dospelých žije v domácnosti?
101007: [
        value: "2",
        type : "enumeration"
],
//                Koľko detí žije v domácnosti?
101008: [
        value: "1",
        type : "enumeration"
],
//                Bolo poistenému alebo spolupoistenej osobe niekedy v minulosti zamiestnuté poistné plnenie alebo vypovedaná poistná zmluva?
101009: [
        value: false,
        type : "boolean"
],
//                Bol poistený alebo spolupoistená osoba niekedy trestné stihaný / vyhlásil osobný bankrot / stíhaný za
101010: [
        value: false,
        type : "boolean"
],
//                Je plánovaná rekonštrukcia alebo prestavba poisťovanej nehnuteľnosti v priebehu najbližších 3 mesiacov?
101011: [
        value: false,
        type : "boolean"
],
//                Žije v poisťovanej domácnosti pes alebo mačka?
101012: [
        value: false,
        type : "boolean"
],
//                Kedy je nehnuteľnosť najviac obývaná?
101013: [
        value: "počas celého dňa",
        type : "boolean"
],
//                Je nehnuteľnosť využívaná aj na podnikanie?
101014: [
        value: false,
        type : "boolean"
],
//                Právna subjektivita poisteného?
101015: [
        value: "fyzická osoba",
        type : "enumeration"
],
//                Počet poistných udalostí za posledné 3 roky?
101016: [
        value: "0",
        type : "enumeration"
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
//                Predmet poistenia
102001: [
        value: "rodinný dom",
        type : "enumeration",
],
//                Poistenie nehnuteľnosti-Spoluúčasť
105005: [
        value: "150.00 €",
        type : "enumeration",
],
//                Podlahová plocha pivnice
105001: [
        value: 100,
        type : "number",
],
//                Podlahová plocha prízemia
105002: [
        value: 100,
        type : "number",
],
//                Podlahová plocha všetkých obytných poschodí
105003: [
        value: 100,
        type : "number",
],
//                Konštrukcia múrov
102002: [
        value: "tehla a/alebo betón",
        type : "enumeration",
],
//                Konštrukcia strechy
102003: [
        value: "plech",
        type : "enumeration",
],
//                Koľko rokov má nehnuteľnosť?
102004: [
        value: "6 až 10",
        type : "enumeration",
],
//                Má nehnuteľnosť praskliny na vonkajšej fasáde?
102005: [
        value: false,
        type : "boolean",
],
//                Koľko izieb má nehnuteľnosť?
102006: [
        value: "viac ako 4",
        type : "enumeration",
],
//                Koľko kúpeľní má nehnuteľnosť?
102007: [
        value: "viac ako 4",
        type : "enumeration",
],
//                Uveďte celkovú hodnotu rekonštrukcií, ak boli vykonané
102008: [
        value: "bez rekonštrukcií",
        type : "enumeration",
],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Poistenie zodpovednosti z vlastníctva nehnuteľnosti
107001: [
        value: "0.00 €",
        type : "enumeration",
],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Doplnkové poistenie nehnuteľnosti"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Doplnkové poistenie nehnuteľnosti" }.getStringId()

        taskService.assignTask(1L, taskID)
        taskService.finishTask(1L, taskID)
    }

    private "Vedľajšie stavby"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Vedľajšie stavby" }.getStringId()

        taskService.assignTask(1L, taskID)
        taskService.finishTask(1L, taskID)
    }

    private "Sumár"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Sumár" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                PERIODICITA PLATBY POISTNÉHO
108001: [
        value: "štvrťročná",
        type : "enumeration"
],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private ObjectNode populateDataset(Map<Long, Map<String, String>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = JsonOutput.toJson(data.collectEntries { [(idConverter.get(it.key as Long)): it.value] })
        return mapper.readTree(json) as ObjectNode
    }

    private void assertCalculation() {
        _case = caseRepository.findOne(_case.getStringId())

//        Poistenie nehnuteľnosti
        assert Math.round(valueOf(308001) * 100) / 100.0 == 369.71
//        Poistenie domácnosti
        assert Math.round(valueOf(308002) * 100) / 100.0 == 0.00
//        Poistenie zodpovednosti za škodu
        assert Math.round(valueOf(308003) * 100) / 100.0 == 0.00
//        ROČNÉ POISTNÉ CELKOM
        assert Math.round(valueOf(308004) * 100) / 100.0 == 369.71
//        Bežné poistné
        assert valueOf(308006) == 351.24
    }

    private def valueOf(Long id) {
        return _case.dataSet[idConverter[id] as String].value
    }
}