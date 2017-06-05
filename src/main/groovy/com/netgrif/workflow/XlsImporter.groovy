package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.FieldType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
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
    private OrganizationRepository organizationRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository

    @Autowired
    private AuthorityRepository roleRepository

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
    private Authority userRole
    private Organization ClientOrg

    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()
        net = importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "Ukladacia jednotka", "FMS")

        Organization FMOrg = new Organization("FM Servis")
        FMOrg = organizationRepository.save(FMOrg)
        ClientOrg = new Organization("Client Company")
        ClientOrg = organizationRepository.save(ClientOrg)

        userRole = new Authority("user")
        userRole = roleRepository.save(userRole)
        Authority roleAdmin = new Authority("admin")
        roleRepository.save(roleAdmin)

        def user = new User(
                email: "super@netgrif.com",
                name: "Super",
                surname: "Trooper",
                password: "password",
                authorities: roleRepository.findAll() as Set,
                userProcessRoles: userProcessRoleRepository.findAll() as Set,
                organizations: organizationRepository.findAll() as Set)
        userService.saveNew(user)

        def client_manager = new User(
                email: "manager@company.com",
                name: "Jano",
                surname: "Mrkvička",
                password: "password",
                authorities: [userRole] as Set<Authority>,
                organizations: [ClientOrg] as Set<Organization>)
        client_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client manager" }.stringId)))
        def client_worker = new User(
                email: "worker@company.com",
                name: "Mária",
                surname: "Kováčová",
                password: "password",
                authorities: [userRole] as Set<Authority>,
                organizations: [ClientOrg] as Set<Organization>)
        client_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "client" }.stringId)))
        def fm_manager = new User(
                email: "manager@fmservis.sk",
                name: "Peter",
                surname: "Molnár",
                password: "password",
                authorities: [roleAdmin] as Set<Authority>,
                organizations: [FMOrg] as Set<Organization>)
        fm_manager.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm manager" }.stringId)))
        def fm_worker = new User(
                email: "worker@fmservis.sk",
                name: "Štefan",
                surname: "Horváth",
                password: "password",
                authorities: [userRole] as Set<Authority>,
                organizations: [FMOrg] as Set<Organization>)
        fm_worker.addProcessRole(userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "fm servis" }.stringId)))
        userService.saveNew(client_manager)
        userService.saveNew(client_worker)
        userService.saveNew(fm_manager)
        userService.saveNew(fm_worker)

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
                            authorities: [userRole] as Set<Authority>,
                            organizations: [ClientOrg] as Set<Organization>
                    )
                    user.addProcessRole(clientRole)
                    user = userService.saveNew(user)
                } else {
                    user.authorities.size()
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
        name = Normalizer.normalize(name, Normalizer.Form.NFD)
        name = name.replaceAll("[^\\p{ASCII}]", "").toLowerCase()
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
        int randomNum = ThreadLocalRandom.current().nextInt(0, 7)
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500"
            case 1:
                return "color-fg-indigo-500"
            case 2:
                return "color-fg-deep-purple-500"
            case 3:
                return "color-fg-lime-500"
            case 4:
                return "color-fg-amber-500"
            case 5:
                return "color-fg-deep-orange-500"
            case 6:
                return "color-fg-blue-grey-500"
            case 7:
                return "color-fg-brown-500"
            default:
                return "color-fg-primary-500"
        }
    }
}