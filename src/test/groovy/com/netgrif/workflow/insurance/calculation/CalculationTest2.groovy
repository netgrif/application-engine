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
class CalculationTest2 {

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
        "Domácnosť"()
        "Doplnkové poistenie domácnosti"()
        "Sumár"()
        assertCalculation()
    }

    private void init() {
        mongoTemplate.getDb().dropDatabase()
        jdbcTemplate.update("TRUNCATE TABLE postal_code")
        userService.saveNew(new User(
                email: "name2.surname@company.com",
                password: "password2",
                name: "name2",
                surname: "surname2",
                authorities: [
                        authorityRepository.findByName("user")?:authorityRepository.save(new Authority("user"))
                ]
        ))
        jsonNodeFactory = JsonNodeFactory.newInstance()
        net = importer.importPetriNet(new File("src/main/resources/petriNets/poistenie_hhi_18_7_2017.xml"), "Household insurance", "HHI")
        _case = workflowService.createCase(net.getStringId(), "Household insurance2", "color", 1L)
        idConverter = net.dataSet.collectEntries { [(it.value.importId): (it.key)] }
        postalCodeImporter.run()
    }

    private void "Nehnuteľnosť a domácnosť"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Nehnuteľnosť a domácnosť" }.getStringId()

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
                        value: "04001",
                        type : "text",
                ],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Mesto
                301005: [
                        value: "Košice",
                        type : "enumeration"
                ],
//                Nachádza sa mimo obce (extravilán)?
                101002: [
                        value: true,
                        type : "boolean"
                ],
//                Bolo miesto poistenia postihnuté povodňou za posledných 10 rokov?
                101003: [
                        value: true,
                        type : "boolean"
                ],
//                Nachádza nehnuteľnosť sa vo vzdialenosti kratšej ako 300 m od vodného toku?
                101004: [
                        value: true,
                        type : "boolean"
                ],
//                Koľko rokov žijete v poisťovanej nehnuteľnosti?
                101005: [
                        value: "menej ako 1",
                        type : "enumeration"
                ],
//                Aký je vzťah poisteného k poisťovanej nehnuteľnosti?
                101006: [
                        value: "vlastník ma hypotéku na nehnuteľnosť",
                        type : "enumeration"
                ],
//                Koľko dospelých žije v domácnosti?
                101007: [
                        value: "3",
                        type : "enumeration"
                ],
//                Koľko detí žije v domácnosti?
                101008: [
                        value: "2",
                        type : "enumeration"
                ],
//                Bolo poistenému alebo spolupoistenej osobe niekedy v minulosti zamiestnuté poistné plnenie alebo vypovedaná poistná zmluva?
                101009: [
                        value: true,
                        type : "boolean"
                ],
//                Bol poistený alebo spolupoistená osoba niekedy trestné stihaný / vyhlásil osobný bankrot / stíhaný za
                101010: [
                        value: false,
                        type : "boolean"
                ],
//                Je plánovaná rekonštrukcia alebo prestavba poisťovanej nehnuteľnosti v priebehu najbližších 3 mesiacov?
                101011: [
                        value: true,
                        type : "boolean"
                ],
//                Žije v poisťovanej domácnosti pes alebo mačka?
                101012: [
                        value: false,
                        type : "boolean"
                ],
//                Kedy je nehnuteľnosť najviac obývaná?
                101013: [
                        value: "cez deň",
                        type : "enumeration"
                ],
//                Je nehnuteľnosť využívaná aj na podnikanie?
                101014: [
                        value: true,
                        type : "boolean"
                ],
//                Právna subjektivita poisteného?
                101015: [
                        value: "právnická osoba",
                        type : "enumeration"
                ],
//                Počet poistných udalostí za posledné 3 roky?
                101016: [
                        value: "1",
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
                        value: "byt",
                        type : "enumeration",
                ],
//                Poistenie nehnuteľnosti-Spoluúčasť
                105005: [
                        value: "50.00 €",
                        type : "enumeration",
                ],
//                Podlahová plocha pivnice
                105001: [
                        value: 10,
                        type : "number",
                ],
//                Podlahová plocha prízemia
                105002: [
                        value: 20,
                        type : "number",
                ],
//                Podlahová plocha všetkých obytných poschodí
                105003: [
                        value: 30,
                        type : "number",
                ],
//                Konštrukcia múrov
                102002: [
                        value: "porobetón (ytong)",
                        type : "enumeration",
                ],
//                Konštrukcia strechy
                102003: [
                        value: "hydroizolačné fólie",
                        type : "enumeration",
                ],
//                Koľko rokov má nehnuteľnosť?
                102004: [
                        value: "viac ako 20",
                        type : "enumeration",
                ],
//                Má nehnuteľnosť praskliny na vonkajšej fasáde?
                102005: [
                        value: true,
                        type : "boolean",
                ],
//                Koľko izieb má nehnuteľnosť?
                102006: [
                        value: "4",
                        type : "enumeration",
                ],
//                Koľko kúpeľní má nehnuteľnosť?
                102007: [
                        value: "1",
                        type : "enumeration",
                ],
//                Uveďte celkovú hodnotu rekonštrukcií, ak boli vykonané
                102008: [
                        value: "do 50 000 EUR",
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
        ObjectNode dataset = populateDataset([
//                Garáž
                105035: [
                        value: true,
                        type: "boolean"
                ],
//                Iné
                105029: [
                        value: true,
                        type: "boolean"
                ]
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Garáž plocha
                105004: [
                        value: 165,
                        type: "number"
                ],
//                Rovnaké miesto ...
                105008: [
                        value: true,
                        type: "boolean"
                ],
//                Garáž poistná sumá
                105007: [
                        value: 49_000,
                        type: "number"
                ],
//                Iné
                105030: [
                        value: 10_000,
                        type: "number"
                ],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Domácnosť"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Domácnosť" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                Umiestnenie domácnosti
                103001: [
                        value: "byt",
                        type: "enumeration"
                ],
//                Poistenie domácnosti
                106001: [
                        value: "50.00 €",
                        type: "enumeration"
                ],
//                Celková podlahová plocha
                106003: [
                        value: 100,
                        type: "number"
                ],
//                Obývanosť domácnosti
                103002: [
                        value: "dočasná",
                        type: "enumeration"
                ],
//                Je nehnuteľnosť, v ktorej sa nachádza poisťovaná domácnosť v blízkom susedstve s inou obývanou nehnuteľnosťou?
                103003: [
                        value: false,
                        type: "boolean"
                ],
//                Je domácnosť zabezpečená funkčným alarmom?
                103004: [
                        value: true,
                        type: "boolean"
                ],
//                Má domácnosť na oknách vo výške do 3 metrov od okolitého terénu mreže / vonkajšie žalúzie alebo rolety?
                103005: [
                        value: true,
                        type: "boolean"
                ]
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Zodpovednost domacnost poistná suma
                107003: [
                        value: "30,000.00 €",
                        type: "enumeration"
                ],
//                Zodpovednost domacnost - územná platnosť
                104003: [
                        value: "Slovenská republika",
                        type: "enumeration"
                ]
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Doplnkové poistenie domácnosti"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Doplnkové poistenie domácnosti" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                Cennosti
                106004: [
                        value: true,
                        type: "boolean"
                ],
//                Umelecké diela
                106006: [
                        value: true,
                        type: "boolean"
                ],
//                Elektronické a optické zariadenia
                106008: [
                        value: true,
                        type: "boolean"
                ],
//                Elektromotory v domácich spotrebičoch
                106014: [
                        value: true,
                        type: "boolean"
                ],
//                Stavebné súčasti domácnosti
                106016: [
                        value: true,
                        type: "boolean"
                ],
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Cennosti
                106005: [
                        value: 5_000,
                        type: "number"
                ],
//                Umelecké diela
                106007: [
                        value: 10_000,
                        type: "number"
                ],
//                Elektronické a optické zariadenia
                106009: [
                        value: 3_000,
                        type: "number"
                ],
//                Elektromotory v domácich spotrebičoch
                106015: [
                        value: 300,
                        type: "number"
                ],
//                Stavebné súčasti domácnosti
                106017: [
                        value: 15_000,
                        type: "number"
                ],
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Sumár"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Sumár" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                POISTNA SUMA Nehnutelnost
                105036: [
                        value: 357_500,
                        type: "number"
                ],
                106022: [
                        value: 357_500,
                        type: "number"
                ]
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                PERIODICITA PLATBY POISTNÉHO
                108001: [
                        value: "polročná",
                        type: "enumeration"
                ],
//                ZĽAVA ZA INÉ POISTENIE V PREMIUM
                108002: [
                        value: false,
                        type: "boolean"
                ],
//                OBCHODNÁ ZĽAVA
                108003: [
                        value: "20%",
                        type: "enumeration"
                ],
//                AKCIOVÁ ZĽAVA
                108004: [
                        value: true,
                        type: "boolean"
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

    @SuppressWarnings("GroovyAssignabilityCheck")
    private void assertCalculation() {
        _case = caseRepository.findOne(_case.getStringId())

//        Základné poistenie
        assert Math.round(valueOf(201017)*100)/100.0 == 7.49
//        Nehnutelnost
        assert Math.round(valueOf(202009)*1000)/1000.0 == 1.937
//        Poistenie nehnuteľnosti
        assert Math.round(valueOf(308001)*100)/100.0 == 5_243.37
//        Poistenie domácnosti
        assert Math.round(valueOf(308002)*100)/100.0 == 22_182.26
//        Poistenie zodpovednosti za škodu
        assert Math.round(valueOf(308003)*100)/100.0 == 9.00
//        ROČNÉ POISTNÉ CELKOM
        assert Math.round(valueOf(308004)*100)/100.0 == 27_434.62
//        Bežné poistné
        assert valueOf(308006) == 17_545.52
    }

    private def valueOf(Long id) {
        return _case.dataSet[idConverter[id] as String].value
    }
}