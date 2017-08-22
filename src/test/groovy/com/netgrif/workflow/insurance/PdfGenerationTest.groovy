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
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Insurance
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
class PdfGenerationTest {

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
        "Údaje o poistníkovi a mieste poistenia"()
        assertPdfGeneration()
    }

    private void init() {
        mongoTemplate.getDb().dropDatabase()
        jdbcTemplate.update("TRUNCATE TABLE postal_code")
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
        value: "81101",
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
        type : "enumeration"
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
        value: "tehla a/alebo betón",
        type : "enumeration",
],
//                Konštrukcia strechy
102003: [
        value: "škridla",
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
        value: "1",
        type : "enumeration",
],
//                Koľko kúpeľní má nehnuteľnosť?
102007: [
        value: "1",
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
        value: "15,000.00 €",
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
        ObjectNode dataset = populateDataset([
//                Stavebné materialy
105031: [
        value: true,
        type: "boolean"
],
//                Stavebné a záhradné mechanizmy
105033: [
        value: true,
        type: "boolean"
]
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Stavebné materiály
105032: [
        value: 1000,
        type: "number"
],
//                Stavebné a záhradné mechanizmy
105034: [
        value: 1000,
        type: "number"
]
        ])
        taskService.setData(taskID, dataset)
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
//                Hospodárska budova
105009: [
        value: true,
        type: "boolean"
],
//                Altánok
105011: [
        value: true,
        type: "boolean"
],
//                Prístrešok
105013: [
        value: true,
        type: "boolean"
],
//                Chodník
105015: [
        value: true,
        type: "boolean"
],
//                Sauna
105017: [
        value: true,
        type: "boolean"
],
//                Elektrická brána
105019: [
        value: true,
        type: "boolean"
],
//                Tenisový kurt
105021: [
        value: true,
        type: "boolean"
],
//                Vonkajší bazén
105023: [
        value: true,
        type: "boolean"
],
//                Studňa
105025: [
        value: true,
        type: "boolean"
],
//                Žumpa, septik
105027: [
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
        value: 100,
        type: "number"
],
//                Rovnaké miesto ...
105008: [
        value: false,
        type: "boolean"
],
//                Garáž poistná sumá
105007: [
        value: 90_000,
        type: "number"
],
//                Hospodárska budova
105010: [
        value: 1000,
        type: "number"
],
//                Altánok
105012: [
        value: 1000,
        type: "number"
],
//                Prístrešok
105014: [
        value: 1000,
        type: "number"
],
//                Chodník
105016: [
        value: 1000,
        type: "number"
],
//                Sauna
105018: [
        value: 1000,
        type: "number"
],
//               Elektrická brána
105020: [
        value: 1000,
        type: "number"
],
//                Tenisový kurt
105022: [
        value: 1000,
        type: "number"
],
//                Vonkajší bazén
105024: [
        value: 1000,
        type: "number"
],
//                Studňa
105026: [
        value: 1000,
        type: "number"
],
//                Žumpa, septik
105028: [
        value: 1000,
        type: "number"
],
//                Iné
105030: [
        value: 1000,
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
        value: "150.00 €",
        type: "enumeration"
],
//                Celková podlahová plocha
106003: [
        value: 100,
        type: "number"
],
//                Obývanosť domácnosti
103002: [
        value: "trvalá",
        type: "enumeration"
],
//                Je nehnuteľnosť, v ktorej sa nachádza poisťovaná domácnosť v blízkom susedstve s inou obývanou nehnuteľnosťou?
103003: [
        value: true,
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
        value: "15,000.00 €",
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
//                Špecialne sklá a presklenie
106010: [
        value: true,
        type: "boolean"
],
//                Záhradné vybavenie a nábytok
106012: [
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
//                Športové náradie
106018: [
        value: true,
        type: "boolean"
],
//                Iné
106020: [
        value: true,
        type: "boolean"
]
        ])
        taskService.setData(taskID, dataset)
        dataset = populateDataset([
//                Cennosti
106005: [
        value: 1000,
        type: "number"
],
//                Umelecké diela
106007: [
        value: 1000,
        type: "number"
],
//                Elektronické a optické zariadenia
106009: [
        value: 1000,
        type: "number"
],
//                Špecialne sklá a presklenie
106011: [
        value: 1000,
        type: "number"
],
//                Záhradné vybavenie a nábytok
106013: [
        value: 1000,
        type: "number"
],
//                Elektromotory v domácich spotrebičoch
106015: [
        value: 1000,
        type: "number"
],
//                Stavebné súčasti domácnosti
106017: [
        value: 1000,
        type: "number"
],
//                Športové náradie
106019: [
        value: 1000,
        type: "number"
],
//                Iné
106021: [
        value: 1000,
        type: "number"
]
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Sumár"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Sumár" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
//                PERIODICITA PLATBY POISTNÉHO
108001: [
        value: "polročná",
        type: "enumeration"
],
//                ZĽAVA ZA INÉ POISTENIE V PREMIUM
108002: [
        value: true,
        type: "boolean"
],
//                OBCHODNÁ ZĽAVA
108003: [
        value: "20%",
        type: "enumeration"
]
        ])
        taskService.setData(taskID, dataset)
        taskService.finishTask(1L, taskID)
    }

    private "Údaje o poistníkovi a mieste poistenia"() {
        List<TaskReference> references = taskService.findAllByCase(_case.getStringId())
        String taskID = references.find { it.getTitle() == "Údaje o poistníkovi a mieste poistenia" }.getStringId()

        taskService.assignTask(1L, taskID)
        ObjectNode dataset = populateDataset([
                109045: [
                        value: "Staré Grunty",
                        type: "text"
                ],
                109046: [
                        value: "53",
                        type: "text"
                ],
                109010: [
                        value: "Jožko",
                        type: "text"
                ],
                109011: [
                        value: "Mrkvička",
                        type: "text"
                ],
                109013: [
                        value: "SK",
                        type: "enumeration"
                ],
                109015: [
                        value: "9302291234",
                        type: "text"
                ],
                109016: [
                        value: "OP",
                        type: "enumeration"
                ],
                109017: [
                        value: "AB123456",
                        type: "text"
                ],
                109018: [
                        value: "+421948 123 456",
                        type: "text"
                ],
                109019: [
                        value: "jozko.mrkvicka@gmail.com",
                        type: "text"
                ]
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

    private void assertPdfGeneration() {
        _case = caseRepository.findOne(_case.getStringId())

        def insurance = new Insurance(_case, _case.petriNet.dataSet[idConverter.get(309003 as Long) as String])
        def file = insurance.offerPDF()

        assert file != null
    }

    private def valueOf(Long id) {
        return _case.dataSet[idConverter[id] as String].value
    }
}