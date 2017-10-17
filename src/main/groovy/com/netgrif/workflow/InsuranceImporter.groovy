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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class InsuranceImporter {

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


    void run(String... strings) throws Exception {
        def net = importer.importPetriNet(new File("src/main/resources/petriNets/insurance_demo.xml"), "Insurance", "INS")

        def orgs = createOrganizations()
        def auths = createAuthorities()
        createUsers(orgs, auths, net)
        createCases(net)
    }

    private Map<String, Organization> createOrganizations() {
        Map<String, Organization> orgs = new HashMap<>()
        orgs.put("insurance", organizationRepository.save(new Organization("Insurance Company")))
        return orgs
    }

    private Map<String, Authority> createAuthorities() {
        Map<String, Authority> auths = new HashMap<>()
        auths.put(Authority.user, authorityRepository.save(new Authority(Authority.user)))
        return auths
    }

    private void createUsers(Map<String, Organization> orgs, Map<String, Authority> auths, PetriNet net) {
        def agentRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "Agent" }.objectId)
        def systemRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "System" }.objectId)

        User agent = new User(
                name: "Agent",
                surname: "Smith",
                email: "agent@company.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        agent.addProcessRole(agentRole)
        User system = new User(
                name: "System",
                surname: "System",
                email: "system@company.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>)
        system.addProcessRole(systemRole)
        userService.saveNew(agent)
        userService.saveNew(system)

        User gabo = new User(
                name: "Gabo",
                surname: "Juhás",
                email: "gabo@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        gabo.addProcessRole(agentRole)
        userService.saveNew(gabo)
        User milan = new User(
                name: "Milan",
                surname: "Mladoniczky",
                email: "mladoniczky@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        milan.addProcessRole(agentRole)
        userService.saveNew(milan)
        User juraj = new User(
                name: "Juraj",
                surname: "Mažári",
                email: "mazari@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        juraj.addProcessRole(agentRole)
        userService.saveNew(juraj)
        User tomas = new User(
                name: "Tomáš",
                surname: "Gažo",
                email: "gazo@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        tomas.addProcessRole(agentRole)
        userService.saveNew(tomas)
        User martin = new User(
                name: "Martin",
                surname: "Makáň",
                email: "makan@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        martin.addProcessRole(agentRole)
        userService.saveNew(martin)
    }

    private void createCases(PetriNet net) {
        Case useCase = new Case(
                title: "Buildings cover",
                petriNet: net,
                color: StartRunner.randomColor())
        useCase.dataSet = new HashMap<>(net.dataSet.collectEntries { [(it.key): new DataField()] })
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "B" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "L" }.key, 1)
        useCase.setAuthor(1L)
        caseRepository.save(useCase)
        net.initializeTokens(useCase.activePlaces)
        taskService.createTasks(useCase)

        useCase = new Case(
                title: "Contents cover",
                petriNet: net,
                color: StartRunner.randomColor())
        useCase.dataSet = new HashMap<>(net.dataSet.collectEntries { [(it.key): new DataField()] })
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "C" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "L" }.key, 1)
        useCase.setAuthor(1L)
        caseRepository.save(useCase)
        net.initializeTokens(useCase.activePlaces)
        taskService.createTasks(useCase)

        useCase = new Case(
                title: "Buildings & contents cover",
                petriNet: net,
                color: StartRunner.randomColor())
        useCase.dataSet = new HashMap<>(net.dataSet.collectEntries { [(it.key): new DataField()] })
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "B" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "L" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "C" }.key, 1)
        useCase.setAuthor(1L)
        useCase = caseRepository.save(useCase)
        net.initializeTokens(useCase.activePlaces)
        taskService.createTasks(useCase)
    }
}
