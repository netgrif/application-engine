package com.fmworkflow;

import com.fmworkflow.auth.domain.*;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.importer.Importer;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import com.fmworkflow.workflow.service.ITaskService;
import com.fmworkflow.workflow.service.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@EnableCaching
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class WorkflowManagementSystemApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(WorkflowManagementSystemApplication.class, args);
	}

	@Autowired
	private IUserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private IWorkflowService workflowService;

	@Autowired
	private ITaskService taskService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
    private Importer importer;

	@Autowired
    private PetriNetRepository petriNetRepository;

	@Override
	public void run(String... strings) throws Exception {
		Role role = new Role("user");
		role = roleRepository.save(role);
		User user = new User("user@fmworkflow.com", "password", "name", "surname");
		HashSet<Role> roles = new HashSet<>();
		roles.add(role);
		user.setRoles(roles);
//		userService.save(user);

		//admin account
		Role adminRole = new Role("admin");
		adminRole = roleRepository.save(adminRole);
		User admin = new User("admin@fmworkflow.com","adminPass","Admin","Adminovič");
		HashSet<Role> adminRoles = new HashSet<>();
		adminRoles.add(adminRole);
		admin.setRoles(adminRoles);
		userService.save(admin);

		mongoTemplate.getDb().dropDatabase();
		importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "fm net", "fm");
        PetriNet net = petriNetRepository.findAll().get(0);
		workflowService.createCase(net.getStringId(), "fm use case", null);
		//workflowService.getAll().forEach(aCase -> taskService.createTasks(aCase));

		User client = new User("client@fmworkflow.com", "password", "name", "surname");
		HashSet<Role> clientRoles = new HashSet<>();
		clientRoles.add(role);
		client.setRoles(clientRoles);

		List<ProcessRole> proles = new LinkedList<>(net.getRoles().values());
		UserProcessRole proleClient = new UserProcessRole();
		proleClient.setRoleId(proles.get(0).getStringId());
		proleClient = userProcessRoleRepository.save(proleClient);
		client.addProcessRole(proleClient);
		userService.save(client);

		UserProcessRole proleFm = new UserProcessRole();
		proleFm.setRoleId(proles.get(1).getStringId());
		proleFm = userProcessRoleRepository.save(proleFm);
		user.addProcessRole(proleFm);
		userService.save(user);
	}

	@Autowired
	private UserProcessRoleRepository userProcessRoleRepository;
}