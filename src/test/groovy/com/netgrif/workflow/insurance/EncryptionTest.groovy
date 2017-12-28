package com.netgrif.workflow.insurance

import com.netgrif.workflow.StartRunner
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class EncryptionTest {

    @Autowired
    private TaskService taskService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private Importer importer

    private PetriNet contactNet

    def benchmark = { closure ->
        def start = System.currentTimeMillis()
        closure.call()
        def now = System.currentTimeMillis()
        return now - start
    }

    @Test
    @Ignore
    void testEncryption() {
        contactNet = importer.importPetriNet(new File("src/main/resources/petriNets/contact.xml"), "Contact", "CON")
        def duration = benchmark {
            (0..1000).inject(0) { sum, item ->
                createContactCase("", "", "", "")
                sum + item
            }
        }
        println "Execution time $duration"
    }

    private void createContactCase(String name, String surname, String telNumber, String email) {
        def contactCase = createCase(name+" "+surname, contactNet, 1L)
        def nameField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Meno"}
        def surnameField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Priezvisko"}
        def telField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Telefónne číslo"}
        def emailField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Email"}
        def rcField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Rodné číslo"}

        contactCase.dataSet.put(nameField.getStringId(), new DataField(name))
        contactCase.dataSet.put(surnameField.getStringId(), new DataField(surname))
        contactCase.dataSet.put(telField.getStringId(), new DataField(telNumber))
        contactCase.dataSet.put(emailField.getStringId(), new DataField(email))
        contactCase.dataSet.put(rcField.getStringId(), new DataField("123456789"))

        caseRepository.save(contactCase)
    }

    private Case createCase(String title, PetriNet net, Long author) {
        Case useCase = new Case(title, net, net.getActivePlaces())
        useCase.setColor(StartRunner.randomColor())
        useCase.setAuthor(author)
        useCase.setIcon(net.icon)
        useCase = caseRepository.save(useCase)
        taskService.createTasks(useCase)
        return useCase
    }
}
