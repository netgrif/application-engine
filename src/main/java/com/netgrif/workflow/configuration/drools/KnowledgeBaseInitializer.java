package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.workflow.configuration.drools.throwable.RuleValidationException;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import org.drools.core.io.impl.ClassPathResource;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeBaseInitializer implements IKnowledgeBaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseInitializer.class);

    private static final DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Value("${drools.template.path}")
    private String templatePath;


    @Autowired
    private RuleRepository ruleRepository;


    @Override
    public KieBase constructKieBase() {
        List<StoredRule> rules = ruleRepository.findAll();
        KieHelper kieHelper = new KieHelper();
        try {
            buildRules(rules, kieHelper);
        } catch (Exception e) {
            log.error("Failed to construct rule engine knowledge base", e);
            throw new IllegalStateException("Rules not successfully loaded");
        }

        return kieHelper.build();
    }

    protected void buildRules(List<StoredRule> rules, KieHelper kieHelper) throws IOException {
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        List<Map<String, String>> ruleAttributes = new ArrayList<>();

        rules.forEach(persistedRule -> {
            Map<String, String> templateRule = new HashMap<>();
            templateRule.put("ruleId", persistedRule.getStringId());
            templateRule.put("ruleEnabled", Boolean.toString(persistedRule.isEnabled()));
            templateRule.put("whenCondition", persistedRule.getWhen());
            templateRule.put("thenAction", persistedRule.getThen());

            String dateEffective = (persistedRule.getDateEffective() == null) ? "" : "date-effective \"" + formatDate(persistedRule.getDateEffective()) + "\"";
            String dateExpires = (persistedRule.getDateExpires() == null) ? "" : "date-expires \"" + formatDate(persistedRule.getDateExpires()) + "\"";

            templateRule.put("salienceVal", Integer.toString(persistedRule.getSalience()));
            templateRule.put("dateEffective", dateEffective);
            templateRule.put("dateExpires", dateExpires);

            ruleAttributes.add(templateRule);
        });

        String generatedDRL;
        try (InputStream template = new ClassPathResource(templatePath).getInputStream()) {
            generatedDRL = compiler.compile(ruleAttributes, template);
        } catch (IOException e) {
            log.error("Failed to compile rules", e);
            throw e;
        }

        KieServices kieServices = KieServices.Factory.get();
        Resource resource1 = kieServices.getResources().newByteArrayResource(generatedDRL.getBytes());
        kieHelper.addResource(resource1, ResourceType.DRL);

        System.out.println("drl:\n" + generatedDRL);
    }

    @Override
    public void validate(List<StoredRule> storedRules) throws RuleValidationException {
        KieSession testSession = null;
        try {
            KieHelper kieHelper = new KieHelper();
            buildRules(storedRules, kieHelper);
            KieBase base = kieHelper.build();;
            testSession = base.newKieSession();
            testSession.fireAllRules();
        } catch (Exception e) {
            log.error("Validation unsuccessful", e);
            throw new RuleValidationException(e);
        } finally {
            if (testSession != null) {
                testSession.destroy();
            }
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(formatter);
    }
}
