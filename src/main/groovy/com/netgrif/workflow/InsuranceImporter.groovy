package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InsuranceImporter {

    private static final Logger log = Logger.getLogger(InsuranceImporter.class.name)

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private IUserService userService

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

    private Map<String, Organization> orgs
    private Map<String, Authority> auths
    private PetriNet insuranceNet
    private PetriNet contactNet
    private PetriNet documentNet

    void run(String... strings) throws Exception {
        log.info("Importing of Petri net Insurance")

        importNets()

        createOrganizations()
        createAuthorities()

        createUsers()
        createCases()
    }

    private importNets() {
        documentNet = importer.importPetriNet(new File("src/main/resources/petriNets/document-lifecycle.xml"), "Dokument", "DOC")
        contactNet = importer.importPetriNet(new File("src/main/resources/petriNets/contact.xml"), "Contact", "CON")
        insuranceNet = importer.importPetriNet(new File("src/main/resources/petriNets/poistenie_hhi_18_7_2017.xml"), "Insurance", "INS")
    }

    private void createOrganizations() {
        log.info("Creating organizations")
        orgs = new HashMap<>()
        orgs.put("insurance", organizationRepository.save(new Organization("Insurance Company")))
    }

    private void createAuthorities() {
        log.info("Creating authorities")
        auths = new HashMap<>()
        auths.put(Authority.user, authorityRepository.save(new Authority(Authority.user)))
    }

    private void createUsers() {
        log.info("Creating users")
        def agentRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Agent" }.objectId
        ))
        def premiumRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Premium" }.objectId
        ))

        def contactRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: contactNet.roles.values().find { it -> it.name == "Agent" }.objectId
        ))
        def documentAdminRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Admin" }.objectId
        ))
        def documentAgentRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Agent" }.objectId
        ))

        User agent = new User(
                name: "Agent",
                surname: "Smith",
                email: "agent@company.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        agent.addProcessRole(agentRole)
        agent.addProcessRole(documentAgentRole)
        agent.addProcessRole(contactRole)
        userService.saveNew(agent)
        log.info("User $agent.name $agent.surname created")

        User premium = new User(
                name: "Premium",
                surname: "Worker",
                email: "user@premium-ic.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        premium.addProcessRole(premiumRole)
        premium.addProcessRole(documentAgentRole)
        userService.saveNew(premium)
        log.info("User $premium.name $premium.surname created")

        User zatko = new User(
                name: "Ondrej",
                surname: "Zaťko",
                email: "ondrej.zatko@premium-ic.sk",
                password: "premiumIC2017",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        zatko.addProcessRole(agentRole)
        zatko.addProcessRole(documentAdminRole)
        userService.saveNew(zatko)
        log.info("User $zatko.name $zatko.surname created")
    }

    private void createCases(){
        createCase("Zmluvné podmienky", documentNet, 4L)

        createContactCase("Adam","Krt", "+421950 123 456", "adam.krt@gmail.com")
        createContactCase("Ežo","Vlkolínsky", "+421902 256 512", "vlkolinsky@gmail.com")
        createContactCase("Jožko", "Mrkvička", "+421948 987 654", "mrkvicka@yahoo.com")

        createCase("Prvé poistenie",insuranceNet,1L)
        Case useCase = createCase("Druhé poistenie",insuranceNet,1L)
    }

    private void createContactCase(String name, String surname, String telNumber, String email) {
        def contactCase = createCase(name+" "+surname, contactNet, 1L)
        def nameField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Meno"}
        def surnameField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Priezvisko"}
        def telField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Telefónne číslo"}
        def emailField = contactCase.petriNet.dataSet.values().find { v -> v.name == "Email"}

        contactCase.dataSet.put(nameField.getObjectId(), new DataField(name))
        contactCase.dataSet.put(surnameField.getObjectId(), new DataField(surname))
        contactCase.dataSet.put(telField.getObjectId(), new DataField(telNumber))
        contactCase.dataSet.put(emailField.getObjectId(), new DataField(email))

        caseRepository.save(contactCase)
    }

    private Case createCase(String title, PetriNet net, Long author) {
        Case useCase = new Case(title, net, net.getActivePlaces())
        useCase.setColor(StartRunner.randomColor())
        useCase.setAuthor(author)
        useCase = caseRepository.save(useCase)
        taskService.createTasks(useCase)
        log.info("Case $title created")
        return useCase
    }
}