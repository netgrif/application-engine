//package com.netgrif.application.engine.startup.runner;
//
//import com.beust.jcommander.Strings;
////import com.netgrif.application.engine.configuration.drools.interfaces.IRefreshableKieBase;
////import com.netgrif.application.engine.configuration.drools.interfaces.IRuleEngineGlobalsProvider;
//import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
//import com.netgrif.application.engine.startup.annotation.RunnerOrder;
//import groovy.text.SimpleTemplateEngine;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.LinkedHashMap;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//@RunnerOrder(30)
//@RequiredArgsConstructor
//public class RuleEngineRunner implements ApplicationEngineStartupRunner {
//
//    @Value("${drools.template.generate:true}")
//    private boolean generate;
//
//    @Value("${drools.template.path:rules/templates/template.drl}")
//    private String generatedTemplatePath;
//
//    @Value("${drools.template-resource.classpath:rules/templates/template.drl}")
//    private String templateResource;
//
////    private final IRefreshableKieBase refreshableKieBase;
////    private final IRuleEngineGlobalsProvider sessionInitializer;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        log.info("Rule engine runner starting");
//        if (generate) {
//            log.info("Generating template to {}", generatedTemplatePath);
//            generateTemplate();
//        }
//        log.info("Loading rules from database");
//        refreshableKieBase.refresh();
//        log.info("Rule engine runner finished");
//    }
//
//    public void generateTemplate() throws IOException, ClassNotFoundException {
//        SimpleTemplateEngine engine = new SimpleTemplateEngine();
//        LinkedHashMap<String, String> binding = new LinkedHashMap<>(2);
//        binding.put("imports", Strings.join("", sessionInitializer.imports()));
//        binding.put("globals", sessionInitializer.globals().stream().map(Object::toString).collect(Collectors.joining("")));
//
//        String template = String.valueOf(engine.createTemplate(new String(new ClassPathResource(templateResource).getInputStream().readAllBytes())).make(binding));
//
//        File templateFile = new File(generatedTemplatePath);
//        templateFile.getParentFile().mkdirs();
//        boolean deleted = templateFile.delete();
//        if (!deleted) {
//            log.warn("Previous generated template file was not deleted");
//        }
//
//        templateFile.createNewFile();
//        if (!templateFile.exists()) {
//            throw new IllegalStateException("Template file " + templateFile.getAbsolutePath() + " was not created");
//        }
//
//        Files.writeString(templateFile.toPath(), template);
//        log.info("Generated template into file ");
//    }
//
//}