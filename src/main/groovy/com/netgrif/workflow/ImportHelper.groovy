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
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
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
    private Importer importer

    @Autowired
    private ResourceLoader resourceLoader

    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Organization> createOrganizations(Map<String, String> organizations) {
        log.info("Creating organizations")
        HashMap<String, Organization> orgsMap = new HashMap<>()
        organizations.each { org ->
            orgsMap.put(org.key, organizationRepository.save(new Organization(org.value)))
        }

        return orgsMap
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Authority> createAuthorities(Map<String, String> authorities) {
        log.info("Creating authorities")
        HashMap<String, Authority> authoritities = new HashMap<>()
        authorities.each { authority ->
            authoritities.put(authority.key, authorityRepository.save(new Authority(authority.value)))
        }

        return authoritities
    }

    private void createProcessRoles() {
        log.info("Creating Process roles")
        processRoles = new HashMap<>()

        processRoles.put("agent", userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Agent" }.stringId)))
        processRoles.put("premium", userProcessRoleRepository.save(new UserProcessRole(
                roleId: insuranceNet.roles.values().find { it -> it.name == "Premium" }.stringId)))
        processRoles.put("contact", userProcessRoleRepository.save(new UserProcessRole(
                roleId: contactNet.roles.values().find { it -> it.name == "Agent" }.stringId)))
        processRoles.put("docAgent", userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Agent" }.stringId)))
        processRoles.put("docAdmin", userProcessRoleRepository.save(new UserProcessRole(
                roleId: documentNet.roles.values().find { it -> it.name == "Admin" }.stringId)))
    }

    UserProcessRole createUserProcessRole(PetriNet net, String name) {
        UserProcessRole role = userProcessRoleRepository.save(new UserProcessRole(roleId:
                net.roles.values().find { it -> it.name == name }.stringId))
        log.info("Created user process role $name")
        return role
    }

    private void createUsers() {
        log.info("Creating users")

        //Generic test users
        createUser(new User(name: "Agent", surname: "Smith", email: "agent@company.com", password: "password"),
                [auths.get(Authority.user), auths.get("permMyContracts"), auths.get("permCreateOffers"), auths.get("permCreateContacts"), auths.get("permDashboard")] as Authority[],
                [orgs.get("insurance")] as Organization[], [processRoles.get("agent"), processRoles.get("contact"), processRoles.get("docAgent")] as UserProcessRole[])

        createUser(new User(name: "Premium", surname: "Employee", email: "user@premium-ic.sk", password: "password"),
                [auths.get(Authority.user), auths.get("permPayments"), auths.get("permActiveContracts")] as Authority[],
                [orgs.get("insurance")] as Organization[], [processRoles.get("premium"), processRoles.get("docAdmin")] as UserProcessRole[])

        //Premium users
        createUser(new User(name: "Ondrej", surname: "Zaťko", email: "ondrej.zatko@premium-ic.sk", password: "premiumIC2017"),
                [auths.get(Authority.user), auths.get("permPayments"), auths.get("permActiveContracts"), auths.get("permMyContracts"), auths.get("permCreateOffers"), auths.get("permCreateContacts"), auths.get("permDashboard")] as Authority[],
                [orgs.get("insurance")] as Organization[], [processRoles.get("agent"), processRoles.get("docAdmin"), processRoles.get("contact")] as UserProcessRole[])

        /* createUser(new User(name: "Ľubomír", surname: "Dzugas", email: "lubomir.dzugas@premium-ic.sk", password: "premiumIC2017"),
                 [auths.get(Authority.admin), auths.get("permPayments"), auths.get("permActiveContracts"), auths.get("permMyContracts"), auths.get("permCreateOffers"), auths.get("permCreateContacts"), auths.get("permDashboard")] as Authority[],
                 [orgs.get("insurance")] as Organization[], [processRoles.get("agent"), processRoles.get("premium"), processRoles.get("docAdmin"), processRoles.get("contact")] as UserProcessRole[])
 */
        //Gratex users
        /* def perms = [auths.get(Authority.user), auths.get("permMyContracts"), auths.get("permCreateOffers"), auths.get("permCreateContacts"), auths.get("permDashboard")] as Authority[]
         def pr = [processRoles.get("agent"), processRoles.get("contact"), processRoles.get("docAgent")] as UserProcessRole[]
         createUser(new User(name: "Renáta", surname: "Petríková", email: "rpetrikova@gratex.com", password: "gratex2017"),
                 perms, [orgs.get("gratex")] as Organization[], pr)

         createUser(new User(name: "Martin", surname: "Blichar", email: "blichar@gratex.com", password: "gratex2017"),
                 perms, [orgs.get("gratex")] as Organization[], pr)

         createUser(new User(name: "Tomáš", surname: "Husár", email: "thusar@gratex.com", password: "gratex2017"),
                 perms, [orgs.get("gratex")] as Organization[], pr)

         createUser(new User(name: "Martin", surname: "Marko", email: "marcus@gratex.com", password: "gratex2017"),
                 perms, [orgs.get("gratex")] as Organization[], pr)*/

    }

    User createUser(User user, Authority[] authorities, Organization[] orgs, UserProcessRole[] roles) {
        authorities.each { user.addAuthority(it) }
        orgs.each { user.addOrganization(it) }
        roles.each { user.addProcessRole(it) }
        user = userService.saveNew(user)
        log.info("User $user.name $user.surname created")
        return user
    }

    Case createCase(String title, PetriNet net, Long author) {
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