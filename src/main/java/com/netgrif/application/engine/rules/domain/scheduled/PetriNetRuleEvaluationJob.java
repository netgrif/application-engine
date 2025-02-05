//package com.netgrif.application.engine.rules.domain.scheduled;
//
//import com.netgrif.core.petrinet.domain.PetriNet;
//import com.netgrif.adapter.petrinet.service.PetriNetService;
//import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
//import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
//import org.quartz.JobExecutionContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class PetriNetRuleEvaluationJob extends RuleJob {
//
//    public static final String NET_ID = "netId";
//
//    private static final Logger log = LoggerFactory.getLogger(PetriNetRuleEvaluationJob.class);
//
//    @Autowired
//    private IRuleEngine ruleEngine;
//
//    @Autowired
//    private PetriNetService petriNetService;
//
//    @Override
//    public void doExecute(JobExecutionContext context) {
//        String netId = getInstanceId(context);
//        log.info("Executing PetriNetRuleEvaluationJob for net " + netId + " of rule " + getRuleIdentifier(context));
//        PetriNet net = petriNetService.getPetriNet(netId);
//        ruleEngine.evaluateRules(net, new ScheduledRuleFact(netId, getRuleIdentifier(context)));
//    }
//
//    @Override
//    public String getInstanceId(JobExecutionContext context) {
//        return (String) context.getJobDetail().getJobDataMap().get(NET_ID);
//    }
//
//}