package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.FieldType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@Component
class XlsImporter {

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
    private AuthorityRepository authorityRepository

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
            ["Oddelenie", FieldType.TEXT],
            ["Pododdelenie", FieldType.TEXT],
            ["Dátum odovzdania", FieldType.DATE],
            ["Odovzdal", FieldType.USER],
            ["Lehota uloženia", FieldType.TEXT],
            ["Registratúrna značka", FieldType.TEXT],
            ["Forma", FieldType.TEXT],
            ["Lokalizácia", FieldType.TEXT],
            ["Čiarový kód", FieldType.TEXT],
            ["Dátum skartácie", FieldType.DATE]
    ]

    private final String EMAIL_TEMPLATE = "@company.com"

    private PetriNet net
    private Case useCase
    private UserProcessRole clientRole
    private Authority userRole
    private Organization ClientOrg


    void run(String... strings) throws Exception {
        net = petriNetRepository.findByTitle("CSOB").first()
        userRole = authorityRepository.findByName(Authority.user)
        ClientOrg = organizationRepository.findAll().first()
        clientRole = userProcessRoleRepository.findAll().first()

        def index = 0
        new File("src/main/resources/hup.csv").splitEachLine(",") { fields ->
            if (index++ == 0)
                return
            useCase = new Case(petriNet: net, title: fields[3], color: StartRunner.randomColor())
            useCase.activePlaces.put(net.places.find { it -> it.value.title == "18" }.key, 1)
            useCase.activePlaces.put(net.places.find { it -> it.value.title == "26" }.key, 1)
            useCase.activePlaces.put(net.places.find { it -> it.value.title == "29" }.key, 1)

            header_fields.eachWithIndex { entry, int i ->
                if (i in [7])
                    return
                putValue(entry[0] as String, getValue(fields[i], entry[1] as FieldType))
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
                DateTimeFormatter full = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                LocalDate date
                try {
                    date = LocalDate.from(full.parse(value))
                } catch (Exception ignored) {
                }
                return date
            case FieldType.USER:
                value = value ?: "anonym"
                User user = userRepository.findBySurname(value)
                if (!user) {
                    user = new User(
                            email: "$value@csob.sk",
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
                user.setPassword(null)
                user.setOrganizations(null)
                user.setAuthorities(null)
                user.setUserProcessRoles(null)
                return user
            default:
                return value
        }
    }

    def putValue(String fieldName, Object value) {
        useCase.dataSet.put(net.dataSet.find { it ->
            it.value.name == fieldName
        }.getKey(), new DataField(value))
    }

    static String randomName() {
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
}