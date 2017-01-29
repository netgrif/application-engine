package com.fmworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@EnableCaching
@SpringBootApplication
@Controller
public class WorkflowManagementSystemApplication {

	@RequestMapping(value = "/{path:[^\\.]*}")
	public String redirect() {
		return "forward:/";
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkflowManagementSystemApplication.class, args);
	}
}