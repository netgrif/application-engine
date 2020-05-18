package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeBaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseInitializer.class);

    @Autowired
    private RuleRepository ruleRepository;

    public KieBase constructKieBase() {
        List<StoredRule> rules = ruleRepository.findAll();
        KieHelper kieHelper = new KieHelper();
        try {
            buildRules(rules, kieHelper);
        } catch (FileNotFoundException e) {
            log.error("Failed to construct rule engine knowledge base", e);
            throw new IllegalStateException("Rules not successfully loaded");
        }
        return kieHelper.build();
    }

    protected void buildRules(List<StoredRule> rules, KieHelper kieHelper) throws FileNotFoundException {
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        List<Map<String, String>> ruleAttributes = new ArrayList<>();

        rules.forEach(persistedRule -> {
            Map<String, String> templateRule = new HashMap<>();
            templateRule.put("ruleId", persistedRule.getStringId());
            templateRule.put("whenCondition", persistedRule.getWhen());
            templateRule.put("thenAction", persistedRule.getThen());
            ruleAttributes.add(templateRule);
        });

        FileInputStream template = new FileInputStream("src/main/resources/rules/templates/template.drl"); // TODO classpath property
        String generatedDRL = compiler.compile(ruleAttributes, template);

        KieServices kieServices = KieServices.Factory.get();
        Resource resource1 = kieServices.getResources().newByteArrayResource(generatedDRL.getBytes());
        kieHelper.addResource(resource1, ResourceType.DRL);

        System.out.println("drl:\n" + generatedDRL);
    }

}
