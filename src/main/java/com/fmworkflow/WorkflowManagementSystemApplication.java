package com.fmworkflow;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class WorkflowManagementSystemApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(WorkflowManagementSystemApplication.class, args);
	}

	@Autowired
	private
	UserRepository userRepository;

	@Override
	public void run(String... strings) throws Exception {
		User user = new User("user", "password", "user@fmworkflow.com");
		userRepository.save(user);
	}
}