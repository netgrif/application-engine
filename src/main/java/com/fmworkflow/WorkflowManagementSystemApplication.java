package com.fmworkflow;

import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.domain.RoleRepository;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserRepository;
import com.fmworkflow.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.HashSet;

@EnableCaching
@SpringBootApplication
public class WorkflowManagementSystemApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(WorkflowManagementSystemApplication.class, args);
	}

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void run(String... strings) throws Exception {
		Role role = new Role("user");
		role = roleRepository.save(role);
		User user = new User("user@fmworkflow.com", "password", "name", "surname");
		HashSet<Role> roles = new HashSet<>();
		roles.add(role);
		user.setRoles(roles);
		userService.save(user);

		//admin account
		Role adminRole = new Role("admin");
		adminRole = roleRepository.save(adminRole);
		User admin = new User("admin@fmworkflow.com","adminPass","Admin","Adminovič");
		HashSet<Role> adminRoles = new HashSet<>();
		adminRoles.add(adminRole);
		admin.setRoles(adminRoles);
		userService.save(admin);
	}
}