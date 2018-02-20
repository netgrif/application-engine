package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.*
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Filter
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import com.netgrif.workflow.workflow.service.interfaces.IFilterService
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

import java.time.LocalDateTime

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
    private PetriNetService petriNetService

    @Autowired
    private ResourceLoader resourceLoader

    @Autowired
    private IFilterService filterService

    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Organization> createOrganizations(Map<String, String> organizations) {
        HashMap<String, Organization> orgsMap = new HashMap<>()
        organizations.each { org ->
            orgsMap.put(org.key, organizationRepository.save(new Organization(org.value)))
        }

        log.info("Created ${orgsMap.size()} organizations")
        return orgsMap
    }

    Organization createOrganization(String name) {
        log.info("Creating Organization $name")
        return organizationRepository.save(new Organization(name))
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Authority> createAuthorities(Map<String, String> authorities) {
        HashMap<String, Authority> authoritities = new HashMap<>()
        authorities.each { authority ->
            authoritities.put(authority.key, authorityRepository.save(new Authority(authority.value)))
        }

        log.info("Creating ${authoritities.size()} authorities")
        return authoritities
    }

    Authority createAuthority(String name) {
        log.info("Creating authorities $name")
        return authorityRepository.save(new Authority(name))
    }

    Optional<PetriNet> createNet(String fileName, String name, String initials, LoggedUser loggedUser) {
        return petriNetService.importPetriNet(new File("src/main/resources/petriNets/$fileName"), name, initials, loggedUser)
    }

    UserProcessRole createUserProcessRole(PetriNet net, String name) {
        UserProcessRole role = userProcessRoleRepository.save(new UserProcessRole(roleId:
                net.roles.values().find { it -> it.name.defaultValue == name }.stringId))
        log.info("Created user process role $name")
        return role
    }

    Map<String, UserProcessRole> createUserProcessRoles(Map<String, String> roles, PetriNet net) {
        HashMap<String, UserProcessRole> userRoles = new HashMap<>()
        roles.each { it ->
            userRoles.put(it.key, createUserProcessRole(net, it.value))
        }

        log.info("Created ${userRoles.size()} process roles")
        return userRoles
    }

    Map<String, UserProcessRole> getProcessRoles(PetriNet net) {
        List<UserProcessRole> roles = userProcessRoleRepository.findAllByNetId(net.stringId)
        Map<String, UserProcessRole> map = [:]
        net.roles.values().each { netRole ->
            map[netRole.name.getDefaultValue()] = roles.find { it.roleId == netRole.stringId }
        }
        return map
    }

    User createUser(User user, Authority[] authorities, Organization[] orgs, UserProcessRole[] roles) {
        authorities.each { user.addAuthority(it) }
        orgs.each { user.addOrganization(it) }
        roles.each { user.addProcessRole(it) }
        user = userService.saveNew(user)
        log.info("User $user.name $user.surname created")
        return user
    }

    Case createCase(String title, PetriNet net, LoggedUser user) {
        Case useCase = new Case(title, net, net.getActivePlaces())
        useCase.setColor(getCaseColor())
        useCase.setAuthor(user.transformToAuthor())
        useCase.setIcon(net.icon)
        useCase.setCreationDate(LocalDateTime.now())
        useCase = caseRepository.save(useCase)
        taskService.createTasks(useCase)
        log.info("Case $title created")
        return useCase
    }

    boolean createFilter(String title, String query, String readable, LoggedUser user) {
        return filterService.saveFilter(new CreateFilterBody(title, Filter.VISIBILITY_PUBLIC, "This filter was created automatically for testing purpose only.", Filter.TYPE_TASK, query, readable), user)
    }

    static String getCaseColor() {
        return "color-fg-amber-500"
    }

}