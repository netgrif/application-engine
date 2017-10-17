package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
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
        def net = importer.importPetriNet(new File("src/main/resources/petriNets/FM verzia 0,2.xml"), "CSOB", "ARC")

        def orgs = createOrganizations()
        def auths = createAuthorities()
        createUsers(orgs, auths, net)
    }

    private Map<String, Organization> createOrganizations() {
        Map<String, Organization> orgs = new HashMap<>()
        orgs.put("csob", organizationRepository.save(new Organization("CSOB")))
        return orgs
    }

    private Map<String, Authority> createAuthorities() {
        Map<String, Authority> auths = new HashMap<>()
        auths.put(Authority.user, authorityRepository.save(new Authority(Authority.user)))
        return auths
    }

    private void createUsers(Map<String, Organization> orgs, Map<String, Authority> auths, PetriNet net) {
        def fmWorkerRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "FM worker" }.objectId)
        def fmManagerRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "FM manager" }.objectId)
        def clientRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "Client" }.objectId)
        def clientManagerRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "Client manager" }.objectId)
        def fmCourierRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "FM courier" }.objectId)
        def suplierRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "Supplier" }.objectId)
        def systemRole = userProcessRoleRepository.findByRoleId(net.roles.values().find { it -> it.name == "System" }.objectId)

        User agent = new User(
                name: "Agent",
                surname: "Smith",
                email: "agent@company.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        agent.addProcessRole(fmWorkerRole)
        userService.saveNew(agent)

        User gabo = new User(
                name: "Gabo",
                surname: "Juhás",
                email: "gabo@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        gabo.addProcessRole(fmWorkerRole)
        userService.saveNew(gabo)
        User milan = new User(
                name: "Milan",
                surname: "Mladoniczky",
                email: "mladoniczky@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        milan.addProcessRole(fmWorkerRole)
        userService.saveNew(milan)
        User juraj = new User(
                name: "Juraj",
                surname: "Mažári",
                email: "mazari@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        juraj.addProcessRole(fmWorkerRole)
        userService.saveNew(juraj)
        User tomas = new User(
                name: "Tomáš",
                surname: "Gažo",
                email: "gazo@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        tomas.addProcessRole(fmWorkerRole)
        userService.saveNew(tomas)
        User martin = new User(
                name: "Martin",
                surname: "Makáň",
                email: "makan@netgrif.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("csob")] as Set<Organization>)
        martin.addProcessRole(fmWorkerRole)
        userService.saveNew(martin)
    }
}