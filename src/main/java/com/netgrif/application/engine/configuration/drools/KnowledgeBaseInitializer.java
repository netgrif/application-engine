package com.netgrif.application.engine.configuration.drools;

import com.netgrif.application.engine.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.application.engine.configuration.drools.throwable.RuleValidationException;
import com.netgrif.application.engine.rules.domain.RuleRepository;
import com.netgrif.application.engine.rules.domain.StoredRule;
import com.netgrif.application.engine.utils.DateUtils;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeBaseInitializer implements IKnowledgeBaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseInitializer.class);

    @Value("${drools.template.path:rules/templates/template.drl}")
    private String templatePath;

    @Value("${drools.compile.page-size:100}")
    private Integer pageSize;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private ObjectDataCompiler compiler;

    @Override
    public KieBase constructKieBase() {
        KieHelper kieHelper = new KieHelper();
        try {
            kieHelper = buildAllRules(kieHelper);
        } catch (Exception e) {
            log.error("Failed to construct rule engine knowledge base", e);
            throw new IllegalStateException("Rules not successfully loaded");
        }

        return kieHelper.build();
    }

    @Override
    public void validate(List<StoredRule> storedRules) throws RuleValidationException {
        KieSession testSession = null;
        try {
            KieHelper kieHelper = new KieHelper();
            buildRules(storedRules, kieHelper);
            KieBase base = kieHelper.build();
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

    protected KieHelper buildAllRules(KieHelper kieHelper) throws IOException {
        Long count = ruleRepository.count();
        long numOfPages = ((count / pageSize) + 1);

        log.debug("Compiling rules, count=" + count + ", pages=" + numOfPages);
        for (int page = 0; page < numOfPages; page++) {
            List<StoredRule> rules = ruleRepository.findAll(PageRequest.of(page, pageSize)).getContent();
            buildRules(rules, kieHelper);
        }

        return kieHelper;
    }

    protected KieHelper buildRules(List<StoredRule> rules, KieHelper kieHelper) throws IOException {
        String generatedDRL = compileRules(rules);
        kieHelper = addRulesResource(generatedDRL, kieHelper);
        log.debug("drl:\n" + generatedDRL);

        return kieHelper;
    }

    protected String compileRules(List<StoredRule> rules) throws IOException {
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

        try (InputStream template = templateInputStream()) {
            return compiler.compile(ruleAttributes, template);
        } catch (IOException e) {
            log.error("Failed to compile rules", e);
            throw e;
        }
    }

    protected KieHelper addRulesResource(String generatedDRL, KieHelper kieHelper) {
        KieServices kieServices = KieServices.Factory.get();
        Resource resource = kieServices.getResources().newByteArrayResource(generatedDRL.getBytes());
        kieHelper.addResource(resource, ResourceType.DRL);
        return kieHelper;
    }

    protected InputStream templateInputStream() throws IOException {
        return new FileInputStream(templatePath);
    }

    private String formatDate(LocalDate date) {
        return date.format(DateUtils.dd_MMM_yyyy);
    }
}
