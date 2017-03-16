package com.fmworkflow;

import com.fmworkflow.auth.domain.*;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.importer.Importer;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import com.fmworkflow.workflow.service.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowCommandLineRunner implements CommandLineRunner {
    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Override
    public void run(String... strings) throws Exception {
        Role roleUser = new Role("user");
        roleUser = roleRepository.save(roleUser);
        User user = new User("user@fmworkflow.com", "password", "name", "surname");
        HashSet<Role> roles = new HashSet<>();
        roles.add(roleUser);
        user.setRoles(roles);

        Role roleAdmin = new Role("admin");
        roleAdmin = roleRepository.save(roleAdmin);
        User admin = new User("admin@fmworkflow.com", "adminPass", "Admin", "Adminovič");
        HashSet<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleAdmin);
        admin.setRoles(adminRoles);
        userService.save(admin);

        mongoTemplate.getDb().dropDatabase();
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "fm net", "fm");
        PetriNet net = petriNetRepository.findAll().get(0);
        for (int i = 0; i < 10; i++) {
            workflowService.createCase(net.getStringId(), "fm use case " + i, null);
        }

        User client = new User("client@client.com", "password", "Client", "Client");
        HashSet<Role> clientRoles = new HashSet<>();
        clientRoles.add(roleUser);
        client.setRoles(clientRoles);

        User clientManager = new User("manager@client.com", "password", "Client", "Manager");
        HashSet<Role> managerRoles = new HashSet<>();
        managerRoles.add(roleUser);
        clientManager.setRoles(managerRoles);

        List<ProcessRole> proles = new LinkedList<>(net.getRoles().values().stream().sorted(Comparator.comparing(ProcessRole::getName)).collect(Collectors.toList()));
        ProcessRole clientRole = proles.get(0);
        ProcessRole clientManagerRole = proles.get(1);
        ProcessRole fmServiceRole = proles.get(2);

        UserProcessRole proleClient = new UserProcessRole();
        proleClient.setRoleId(clientRole.getStringId());
        proleClient = userProcessRoleRepository.save(proleClient);
        client.addProcessRole(proleClient);
        userService.save(client);

        UserProcessRole proleFm = new UserProcessRole();
        proleFm.setRoleId(fmServiceRole.getStringId());
        proleFm = userProcessRoleRepository.save(proleFm);
        user.addProcessRole(proleFm);
        userService.save(user);

        UserProcessRole proleManager = new UserProcessRole();
        proleManager.setRoleId(clientManagerRole.getStringId());
        proleManager = userProcessRoleRepository.save(proleManager);
        clientManager.addProcessRole(proleManager);
        userService.save(clientManager);
    }
}