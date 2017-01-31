package com.fmworkflow;

import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.domain.RoleRepository;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserRepository;
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
	private
	UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void run(String... strings) throws Exception {
		Role role = new Role("user");
		role = roleRepository.save(role);
		User user = new User("user", "$2a$10$fJw3A2yoqyE0t31mOECcCOTTuRw7/GlAL8qdnmYxln596D0yQ4toi", "user@fmworkflow.com");
		HashSet<Role> roles = new HashSet<>();
		roles.add(role);
		user.setRoles(roles);
		userRepository.save(user);
	}
}