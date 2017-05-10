package com.fmworkflow

import com.fmworkflow.auth.domain.Role
import com.fmworkflow.auth.domain.User
import com.fmworkflow.auth.domain.UserProcessRole
import com.fmworkflow.auth.domain.repositories.RoleRepository
import com.fmworkflow.auth.domain.repositories.UserProcessRoleRepository
import com.fmworkflow.auth.domain.repositories.UserRepository
import com.fmworkflow.auth.service.interfaces.IUserService
import com.fmworkflow.importer.Importer
import com.fmworkflow.petrinet.domain.PetriNet
import com.fmworkflow.petrinet.domain.dataset.FieldType
import com.fmworkflow.petrinet.domain.repositories.PetriNetRepository
import com.fmworkflow.workflow.domain.Case
import com.fmworkflow.workflow.domain.repositories.CaseRepository
import com.fmworkflow.workflow.service.TaskService
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
    private IUserService userService

    @Autowired
    private UserRepository userRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TaskService taskService

    @Autowired
    private Importer importer

    def email_id = 1
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
            ["Lehota uloženia", FieldType.TEXT],
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
        net = importer.importPetriNet(new File("src/test/resources/prikladFM_import.xml"), "Archiv", "FMS")

        def user = userRepository.findByEmail("super@netgrif.com")
        user.setUserProcessRoles(userProcessRoleRepository.findAll() as Set)
        user.setRoles(roleRepository.findAll() as Set)
        userService.save(user)

        def userRole = roleRepository.findByName("user")
        def client_manager = new User(
                email: "manager@client.com",
                name: "Manager",
                surname: "Client",
                password: "password",
                roles: [userRole] as Set<Role>)
        client_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client manager" }.stringId)))
        def client_worker = new User(
                email: "worker@client.com",
                name: "Worker",
                surname: "Client",
                password: "password",
                roles: [userRole] as Set<Role>)
        client_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client" }.stringId)))
        def fm_manager = new User(
                email: "manager@fmservis.com",
                name: "Manager",
                surname: "FM Servis",
                password: "password",
                roles: [userRole] as Set<Role>)
        fm_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm servis" }.stringId)))
        def fm_worker = new User(
                email: "worker@fmservis.com",
                name: "Worker",
                surname: "FM Servis",
                password: "password",
                roles: [userRole] as Set<Role>)
        fm_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm servis" }.stringId)))
        userService.save(client_manager)
        userService.save(client_worker)
        userService.save(fm_manager)
        userService.save(fm_worker)

        def index = 0
        new File("src/test/resources/Vzor preberacieho protokolu.csv").splitEachLine("\t") { fields ->
            useCase = new Case(petriNet: net, title: "Ukladacia jednotka ${fields[1]}")


            if (index < 10) {
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "B" }.key, 1)

                header_fields.eachWithIndex { entry, int i ->
                    if (i in [0, 5, 8, 14,])
                        return
                    putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
                }
            } else if (index < 20) {
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "K" }.key, 1)

                header_fields.eachWithIndex { entry, int i ->
                    if (i in [0, 5, 8, 14,])
                        return
                    putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
                }
            } else {
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "J" }.key, 1)

                header_fields.eachWithIndex { entry, int i ->
                    putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
                }
            }

            caseRepository.save(useCase)
            taskService.createTasks(useCase)

            index++
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
                            email: "user${email_id++}${EMAIL_TEMPLATE}",
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