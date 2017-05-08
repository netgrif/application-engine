package com.fmworkflow

import com.fmworkflow.auth.domain.User
import com.fmworkflow.auth.domain.repositories.UserRepository
import com.fmworkflow.importer.Importer
import com.fmworkflow.petrinet.domain.PetriNet
import com.fmworkflow.petrinet.domain.dataset.FieldType
import com.fmworkflow.petrinet.domain.repositories.PetriNetRepository
import com.fmworkflow.workflow.domain.Case
import com.fmworkflow.workflow.domain.repositories.CaseRepository
import com.fmworkflow.workflow.service.TaskService
import com.fmworkflow.workflow.service.WorkflowService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@Profile("!test")
class XlsImporter implements CommandLineRunner {

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private UserRepository userRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskService taskService

    @Autowired
    private Importer importer

    def header_fields = [
            ["Box", FieldType.TEXT],
            ["Ukl. jednotka", FieldType.TEXT],
            ["Čísla spisov", FieldType.TEXT],
            ["Vecný obsah UJ", FieldType.TEXT],
            ["Od (rok)", FieldType.DATE],
            ["Do (rok)", FieldType.DATE],
            ["Firma", FieldType.TEXT],
            ["Oddelenie", FieldType.TEXT],
            ["Odovzdávajúci", FieldType.USER],
            ["Dátum odovzdania", FieldType.DATE],
            ["Forma", FieldType.TEXT],
            ["LU", FieldType.TEXT],
            ["Znak hodnoty", FieldType.TEXT],
            ["Čiarový kód", FieldType.TEXT],
            ["Lokalizácia", FieldType.TEXT],
            ["Poznámka", FieldType.TEXT],
            ["Číslo plomby", FieldType.TEXT]
    ]

    private final String EMAIL_TEMPLATE = "@fmservis.sk"

    private PetriNet net
    private Case useCase

    @Override
    void run(String... strings) throws Exception {
        net = importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "Archiv", "FMS")

        new File("src/test/resources/Vzor preberacieho protokolu.csv").splitEachLine("\t") { fields ->
            useCase = new Case(petriNet: net, title: "Ukladacia jednotka ${fields[1]}")

            useCase.activePlaces.put(net.places.find { it -> it.value.title == "A" }.key, 1)

            header_fields.eachWithIndex { entry, int i ->
                putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
            }

            caseRepository.save(useCase)
            taskService.createTasks(useCase)
        }
    }

    private Object getValue(String value, FieldType type) {
        switch (type) {
            case FieldType.DATE:
                DateTimeFormatter full = DateTimeFormatter.ofPattern("d.M.yyyy")
                LocalDate date
                try {
                    date = LocalDate.from(full.parse(value))
                } catch (Exception ignored) {
                    date = LocalDate.ofYearDay(Integer.parseInt(value), 1)
                }
                return date
            case FieldType.USER:
                User user = userRepository.findBySurname(value)
                if (!user) {
                    user = userRepository.save(new User(
                            email: "user${EMAIL_TEMPLATE}",
                            password: "password",
                            name: "Jozko",
                            surname: value,
                    ))
                } else {
                    user.roles.size()
                    user.userProcessRoles.size()
                }
                return [user.getEmail(), user.getFullName()] as List
            default:
                return value
        }
    }

    def putValue(String fieldName, Object value) {
        useCase.dataSetValues.put(net.dataSet.find { it ->
            it.value.name == fieldName
        }.getKey(), value)
    }
}