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
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@Component
@Profile("!test")
class XlsImporter implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate

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

    private final String EMAIL_TEMPLATE = "@company.com"

    private PetriNet net
    private Case useCase
    private UserProcessRole clientRole
    private Role userRole

    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()
        net = importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "Ukladacia jednotka", "FMS")

        userRole = new Role("user")
        userRole = roleRepository.save(userRole)
        Role roleAdmin = new Role("admin")
        roleRepository.save(roleAdmin)

        def user = new User(
                email: "super@netgrif.com",
                name: "Super",
                surname: "Trooper",
                password: "password",
                roles: roleRepository.findAll() as Set,
                userProcessRoles: userProcessRoleRepository.findAll() as Set)
        userService.save(user)

        def client_manager = new User(
                email: "manager@company.com",
                name: "Jano",
                surname: "Mrkvička",
                password: "password",
                roles: [userRole] as Set<Role>)
        client_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client manager" }.stringId)))
        def client_worker = new User(
                email: "worker@company.com",
                name: "Mária",
                surname: "Kováčová",
                password: "password",
                roles: [userRole] as Set<Role>)
        client_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client" }.stringId)))
        def fm_manager = new User(
                email: "manager@fmservis.sk",
                name: "Peter",
                surname: "Molnár",
                password: "password",
                roles: [roleAdmin] as Set<Role>)
        fm_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm manager" }.stringId)))
        def fm_worker = new User(
                email: "worker@fmservis.sk",
                name: "Štefan",
                surname: "Horváth",
                password: "password",
                roles: [userRole] as Set<Role>)
        fm_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm servis" }.stringId)))
        userService.save(client_manager)
        userService.save(client_worker)
        userService.save(fm_manager)
        userService.save(fm_worker)

        clientRole = client_worker.userProcessRoles.first()

        def index = 0
        new File("src/test/resources/Vzor preberacieho protokolu.csv").splitEachLine("\t") { fields ->
            useCase = new Case(petriNet: net, title: fields[3], color: randomColor())


            if (index < 10) {
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "B" }.key, 1)

                header_fields.eachWithIndex { entry, int i ->
                    if (i in [0, 5, 8, 14,])
                        return
                    putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
                }
            } else if (index < 20) {
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "K" }.key, 1)
                useCase.activePlaces.put(net.places.find { it -> it.value.title == "H" }.key, 1)

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
            net.initializeTokens(useCase.activePlaces)
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
                    user = new User(
                            email: generateEmail(value),
                            password: "password",
                            name: randomName(),
                            surname: value,
                            roles: [userRole] as Set<Role>
                    )
                    user.addProcessRole(clientRole)
                    user = userRepository.save(user)
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

    String generateEmail(String name) {
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("[^\\p{ASCII}]", "")
        return name + EMAIL_TEMPLATE
    }

    String randomName() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5)
        switch (randomNum) {
            case 0:
                return "Ema"

            case 1:
                return "Jana"

            case 2:
                return "Natália"

            case 3:
                return "Katarína"

            case 4:
                return "Zuzana"
            default:
                return "Eva"
        }
    }

    private String randomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 7);
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500";
            case 1:
                return "color-fg-indigo-500";
            case 2:
                return "color-fg-deep-purple-500";
            case 3:
                return "color-fg-lime-500";
            case 4:
                return "color-fg-amber-500";
            case 5:
                return "color-fg-deep-orange-500";
            case 6:
                return "color-fg-blue-grey-500";
            case 7:
                return "color-fg-brown-500";
            default:
                return "color-fg-primary-500";
        }
    }
}