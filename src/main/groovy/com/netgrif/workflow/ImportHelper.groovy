package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ImportHelper {

    private static final Logger log = Logger.getLogger(ImportHelper.class.name)

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
    private ImportHelper importer

    private Map<String, Organization> orgs
    private Map<String, Authority> auths
    private PetriNet insuranceNet
    private PetriNet contactNet
    private PetriNet documentNet

    @SuppressWarnings("GroovyAssignabilityCheck")
    void createOrganizations(Map<String, String> organizations) {
        log.info("Creating organizations")
        orgs = new HashMap<>()
        organizations.each { org ->
            orgs.put(org.key, organizationRepository.save(new Organization(org.value)))
        }
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    private void createAuthorities(List<Authority> authorities) {
        log.info("Creating authorities")
        auths = new HashMap<>()
        authorities.each { authority ->
            auths.put(authority, authorityRepository.save(new Authority(authority)))
        }
    }

    private void createUsers() {
        log.info("Creating users")
        def agentRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Agent" }.stringId
        ))
        def premiumRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Premium" }.stringId
        ))

        def contactRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: contactNet.roles.values().find { it -> it.name == "Agent" }.stringId
        ))
        def documentAdminRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Admin" }.stringId
        ))
        def documentAgentRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Agent" }.stringId
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
        zatko.addProcessRole(contactRole)
        zatko.addProcessRole(documentAdminRole)
        userService.saveNew(zatko)
        log.info("User $zatko.name $zatko.surname created")

        User dzugas = new User(
                name: "Ľubomír",
                surname: "Dzugas",
                email: "lubomir.dzugas@premium-ic.sk",
                password: "premiumIC2017",
                authorities: [auths.get(Authority.admin)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        dzugas.addProcessRole(agentRole)
        dzugas.addProcessRole(premiumRole)
        dzugas.addProcessRole(contactRole)
        dzugas.addProcessRole(documentAdminRole)
        userService.saveNew(dzugas)
        log.info("User $dzugas.name $dzugas.surname created")

        User gratex = new User(
                name: "Gratex",
                surname: "International",
                email: "gratex@gratex.com",
                password: "gratex2017",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        gratex.addProcessRole(agentRole)
        gratex.addProcessRole(documentAdminRole)
        userService.saveNew(gratex)
        log.info("User $gratex.name $gratex.surname created")
    }

    private void createCases() {
        createCase("Zmluvné podmienky", documentNet, 4L)
        createCase("Prvé poistenie", insuranceNet, 1L)
        Case useCase = createCase("Druhé poistenie", insuranceNet, 1L)
    }

    private Case createCase(String title, PetriNet net, Long author) {
        Case useCase = new Case(title, net, net.getActivePlaces())
        useCase.setColor(StartRunner.randomColor())
        useCase.setAuthor(author)
        useCase.setIcon(net.icon)
        useCase = caseRepository.save(useCase)
        taskService.createTasks(useCase)
        log.info("Case $title created")
        return useCase
    }
}