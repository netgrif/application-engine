package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.auth.service.UserService
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.domain.GroupRepository
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FieldType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.startup.RunnerController
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

import java.util.concurrent.ThreadLocalRandom

@Component
class JMeterExport {

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserService userService

    @Autowired
    private AuthorityRepository authorityRepository

    @Autowired
    private UserProcessRoleRepository processRoleRepository

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskService taskService

    @Autowired
    private GroupRepository groupRepository

    private PetriNet net
    private Group org
    private Authority authority
    private UserProcessRole processRole

    private long lastMail = 0

    def run(String... strings) {
        net = petriNetRepository.findAll().first()
        org = groupRepository.findAll().first()
        authority = authorityRepository.findByName(Authority.user)
        processRole = processRoleRepository.findByRoleIdIn([net.roles.values().find { it -> it.name == "Agent" }.stringId]).first()

        List<User> users = createUsers(100)
        List<Case> cases = createCases(users)
        List<Task> tasks = getTasks(cases)

        Map<String, List<Field>> tasksFields = new HashMap<>()
        tasks.each {task ->
            List<Field> fields = new ArrayList<>()
            fields.add(getData(task.transitionId,FieldType.DATE))
            fields.add(getData(task.transitionId,FieldType.NUMBER))
            fields.add(getData(task.transitionId,FieldType.TEXT))
            fields.add(getData(task.transitionId,FieldType.NUMBER))
            tasksFields.put(task.stringId,fields)
        }

        def file = new File('perform_test.csv')
        file.text = ""
        file << "AUTH,USERID,NETID,NEWCASE,CASEID,TASKID,DATAID1,DATAID2,DATAID3,DATAID4\n"
        users.eachWithIndex { User user, int i ->
            file << "${user.email}:password".bytes.encodeBase64().toString()
            file << ","
            file << user.id
            file << ","
            file << net.stringId
            file << ","
            file << "Generated "+i
            file << ","
            file << cases.get(i).stringId
            file << ","
            file << tasks.get(i).stringId
            file << ","
            tasksFields.get(tasks.get(i).stringId).eachWithIndex { Field f, int j ->
                file << f.stringId
                if(j < 3) file << ","
            }
            file << "\n"
        }
    }

    List<User> createUsers(int n) {
        List<User> users = new ArrayList<>()
        n.times {
            User user = new User(
                    name: randomName(),
                    surname: randomSurname(),
                    password: "password",
                    authorities: [authority] as Set<Authority>)
            user.addProcessRole(processRole)
            user.setEmail(generateMail(user.name, user.surname))
            user = userService.saveNew(user)
            users.add(user)
        }
        users
    }

    List<Task> getTasks(List<Case> cases){
        List<Task> tasks = new ArrayList<>()
        cases.each {useCase -> tasks.add(getTask(useCase))}
        return tasks
    }

    Task getTask(Case useCase) {
        Page<Task> tasks = taskService.findByCases(new PageRequest(0,20,null), [useCase.stringId] as List)
        return tasks.content.find { task -> task.title == "Personal details" }
    }

    Field getData(String transition, FieldType type) {
        Field f = null
        net.transitions.get(transition).dataSet.find { data ->
            if (net.dataSet.get(data.key).type == type) {
                f = net.dataSet.get(data.key)
                return true
            }
            return false
        }
        return f
    }

    List<Case> createCases(List<User> users) {
        List<Case> cases = new ArrayList<>()
        users.eachWithIndex { User user, int i ->
            cases.add(createCase(user, i))
        }
        return cases
    }

    Case createCase(User author, long index) {
        Case useCase = new Case(
                title: "Buildings cover " + index,
                petriNet: net,
                color: RunnerController.randomColor())
        useCase.dataSet = new HashMap<>(net.dataSet.collectEntries { [(it.key): new DataField()] })
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "B" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "L" }.key, 1)
        useCase.activePlaces.put(net.places.find { it -> it.value.title == "D" }.key, 1)
        useCase.setAuthor(author.id)
        useCase = caseRepository.save(useCase)
        net.initializeTokens(useCase.activePlaces)
        taskService.createTasks(useCase)

        return useCase
    }

    String randomName() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 9)
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
            case 5:
                return "Igor"
            case 6:
                return "Peter"
            case 7:
                return "Juraj"
            case 8:
                return "Eva"
        }
        return "Milan"
    }

    String randomSurname() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 9)
        switch (randomNum) {
            case 0:
                return "Pokorná"
            case 1:
                return "Kováč"
            case 2:
                return "Makáň"
            case 3:
                return "Smith"
            case 4:
                return "Mrkva"
            case 5:
                return "Zemiak"
            case 6:
                return "Juhás"
            case 7:
                return "Topol"
            case 8:
                return "Nagy"
        }
        return "Milanovic"
    }

    String generateMail(String name, String surname) {
        lastMail++
        return name + surname + lastMail + "@company.com"
    }
}
