package com.netgrif.workflow;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.importer.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

//@Component
//@Profile({"!test"})
public class WorkflowCommandLineRunner {//implements CommandLineRunner {
    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository petriNetRepository;


    public void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase();
        // TODO: 26/04/2017 title, initials
        //importer.importPetriNet(new File("src/test/resources/poistenie_rozsirene.xml"), "Poistenie", "INS");
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"),"FM Servis","FM");
        PetriNet net = petriNetRepository.findAll().get(0);
//        for (int i = 0; i < 10; i++) {
//            workflowService.createCase(net.getStringId(), " " + i, randomColor());
//        }

//        Authority authorityUser = new Authority("user");
//        authorityUser = roleRepository.save(authorityUser);
//        User user = new User("poistenec@gmail.com", "password", "name", "surname");
//        HashSet<Authority> roles = new HashSet<>();
//        roles.add(authorityUser);
//        user.setAuthorities(roles);
//        userService.save(user);
//
//        Authority authorityAdmin = new Authority("admin");
//        authorityAdmin = roleRepository.save(authorityAdmin);
//        User admin = new User("agent@gmail.com", "pass", "Admin", "Adminovič");
//        HashSet<Authority> adminRoles = new HashSet<>();
//        adminRoles.add(authorityAdmin);
//        admin.setAuthorities(adminRoles);
//        userService.save(admin);
//
//        User superAdmin = new User("super@netgrif.com", "password", "Super", "Truuper");
//        HashSet<Authority> superRoles = new HashSet<>();
//        superRoles.add(authorityAdmin);
//        superAdmin.setAuthorities(superRoles);
//
//        User client = new User("client@client.com", "password", "Client", "Client");
//        HashSet<Authority> clientRoles = new HashSet<>();
//        clientRoles.add(authorityUser);
//        client.setAuthorities(clientRoles);
//
//        User clientManager = new User("agent@agent.com", "password", "Agent", "Smith");
//        HashSet<Authority> managerRoles = new HashSet<>();
//        managerRoles.add(authorityUser);
//        clientManager.setAuthorities(managerRoles);
//
//        List<ProcessRole> proles = new LinkedList<>(net.getAuthorities().values().stream().sorted(Comparator.comparing(ProcessRole::getName)).collect(Collectors.toList()));
//        ProcessRole clientRole = proles.get(0);
//        ProcessRole clientManagerRole = proles.get(1);
//
//        UserProcessRole proleClient = new UserProcessRole();
//        proleClient.setRoleId(clientRole.getStringId());
//        proleClient = userProcessRoleRepository.save(proleClient);
//        client.addProcessRole(proleClient);
//        superAdmin.addProcessRole(proleClient);
//        userService.save(client);
//       // userService.save(superAdmin);
//
//        UserProcessRole proleFm = new UserProcessRole();
//        proleFm.setRoleId(clientManagerRole.getStringId());
//        proleFm = userProcessRoleRepository.save(proleFm);
//        user.addProcessRole(proleFm);
//        superAdmin.addProcessRole(proleFm);
//        userService.save(user);
//       // userService.save(superAdmin);
//
//        UserProcessRole proleManager = new UserProcessRole();
//        proleManager.setRoleId(clientManagerRole.getStringId());
//        proleManager = userProcessRoleRepository.save(proleManager);
//        clientManager.addProcessRole(proleManager);
//        superAdmin.addProcessRole(proleManager);
//        userService.save(clientManager);
//        userService.save(superAdmin);

        Authority authorityUser = new Authority("user");
        authorityUser = authorityRepository.save(authorityUser);
        Authority authorityAdmin = new Authority("admin");
        authorityAdmin = authorityRepository.save(authorityAdmin);
        List<ProcessRole> proles = new LinkedList<>(net.getRoles().values().stream().sorted(Comparator.comparing(ProcessRole::getName)).collect(Collectors.toList()));

        List<ProcessRole> usePRoles = new LinkedList<>();
//        usePRoles.add(proles.get(0));
//        createUser(new User("poistenec@gmail.com", "password", "Fero", "Poistenec"),authorityUser,usePRoles);
//        usePRoles.add(proles.get(1));
//        createUser(new User("agent@gmail.com", "password", "Jano", "Poisťovák"),authorityAdmin,usePRoles);
//        createUser(new User("super@netgrif.com","password","Super","Trooper"), authorityAdmin, proles);
        createUser(new User("client@gmail.com","password","Mária","Kováčová"), authorityUser,usePRoles);
        createUser(new User("manager.client@gmail.com","password","Jano","Mrkvička"), authorityUser,usePRoles);
        createUser(new User("employee@fmservis.sk","password","Štefan","Horváth"), authorityUser,usePRoles);
        createUser(new User("manager@fmservis.sk","password","Peter","Molnár"), authorityAdmin,usePRoles);
        createUser(new User("super@netgrif.com","password","Super","Trooper"), authorityAdmin, proles);
    }

    private void createUser(User user, Authority authority, List<ProcessRole> processRoles){
        HashSet<Authority> authoritySet = new HashSet<>();
        authoritySet.add(authority);
        user.setAuthorities(authoritySet);

        processRoles.forEach(processRole -> {
            UserProcessRole userProcessRole = new UserProcessRole();
            userProcessRole.setRoleId(processRole.getStringId());
            userProcessRole = userProcessRoleRepository.save(userProcessRole);
            user.addProcessRole(userProcessRole);
        });
        processRoles.clear();

        userService.save(user);
    }

    private String randomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 7);
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500";
            case 1:
                return "color-fg-indigo-500";
            case 2:
                return "color-fg-deep-purple-500";
            case 3:
                return "color-fg-lime-500";
            case 4:
                return "color-fg-amber-500";
            case 5:
                return "color-fg-deep-orange-500";
            case 6:
                return "color-fg-blue-grey-500";
            case 7:
                return "color-fg-brown-500";
            default:
                return "color-fg-primary-500";
        }
    }
}
