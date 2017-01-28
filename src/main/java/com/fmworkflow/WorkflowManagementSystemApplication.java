package com.fmworkflow;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootApplication
public class WorkflowManagementSystemApplication {

	@Bean
	public JavaMailSenderImpl mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("mail.fmworkflow.sk"); // TODO: 27. 1. 2017
		return mailSender;
	}

	@Bean
	public VelocityEngine velocityEngine() {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("resource.loader", "class");
		engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return engine;
	}

	@Bean
	public MailSenderService mailSenderService(JavaMailSenderImpl mailSender, VelocityEngine velocityEngine) {
		MailSenderService mailSenderService = new MailSenderService();
		mailSenderService.setMailSender(mailSender);
		mailSenderService.setVelocityEngine(velocityEngine);
		return mailSenderService;
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkflowManagementSystemApplication.class, args);
	}
}