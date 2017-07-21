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
  //      def net = importer.importPetriNet(new File("src/test/resources/datagroup_test.xml"), "Insurance", "INS")
       def net = importer.importPetriNet(new File("src/main/resources/petriNets/poistenie_hhi_18_7_2017.xml"), "Insurance", "INS")

        def orgs = createOrganizations()
        def auths = createAuthorities()
        createUsers(orgs,auths,net)
        createCases(net)
    }

    private Map<String, Organization> createOrganizations(){
        Map<String, Organization> orgs = new HashMap<>()
        orgs.put("insurance",organizationRepository.save(new Organization("Insurance Company")))
        return orgs
    }

    private Map<String, Authority> createAuthorities(){
        Map<String, Authority> auths = new HashMap<>()
        auths.put(Authority.user,authorityRepository.save(new Authority(Authority.user)))
        return auths
    }

    private void createUsers(Map<String, Organization> orgs, Map<String, Authority> auths, PetriNet net){
        def agentRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "Agent" }.objectId
        ))
        def premiumRole = userProcessRoleRepository.save(new UserProcessRole(
                roleId: net.roles.values().find { it -> it.name == "Premium" }.objectId
        ))

        User agent = new User(
                name: "Agent",
                surname: "Smith",
                email: "agent@company.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        agent.addProcessRole(agentRole)
        userService.saveNew(agent)

        User premium = new User(
                name: "Premium",
                surname: "Worker",
                email: "user@premium-ic.com",
                password: "password",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        premium.addProcessRole(premiumRole)
        userService.saveNew(premium)

        User zatko = new User(
                name: "Ondrej",
                surname: "Zaťko",
                email: "ondrej.zatko@premium-ic.sk",
                password: "premiumIC2017",
                authorities: [auths.get(Authority.user)] as Set<Authority>,
                organizations: [orgs.get("insurance")] as Set<Organization>)
        zatko.addProcessRole(agentRole)
        userService.saveNew(zatko)
    }

    private void createCases(PetriNet net){
        createCase("Prvé poistenie",net,1L)
        Case useCase = createCase("Druhé poistenie",net,1L)

//        def field = net.dataSet.find {it.value.name == "How many adults 18 or over live in the property"}.value
//        field.value = 5
//        def js = FieldValidationRunner.toJavascript(field,field.validationRules)
//        def valid = FieldValidationRunner.validate(field,field.validationRules)
//        field.validationJS(js)

        //def field = useCase.petriNet.dataSet.values().find { v -> v.name == "Ponuka PDF"}
        //def file = new Insurance(useCase,(FileField)field).offerPDF()
        //println file
    }

    private Case createCase(String title, PetriNet net, Long author){
        Case useCase = new Case(title,net,net.getActivePlaces())
        useCase.setColor(StartRunner.randomColor())
        useCase.setAuthor(author)
        useCase = caseRepository.save(useCase)
        taskService.createTasks(useCase)
        return useCase
    }
}
