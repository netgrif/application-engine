package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.quartz.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunner)

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private Scheduler scheduler

    @Override
    void run(String... strings) throws Exception {
//        helper.createNet("mortgage/address.xml","address","Address","ADD","major")
        def insuranceNet = helper.createNet("insurance_portal_demo.xml","insurance","Insurance","INS","major")
        helper.createNet("leukemia.xml", "protokol_leukemia", "Protokol o začatí a kontrole liečby chronickej myelocytovej leukémie", "LEU", "major")

        try {
            helper.createCase("Insurance Case", insuranceNet.get())
        } catch (Exception e) {
            log.error("Failed ", e)
        }

        superCreator.setAllToSuperUser()

        JobDetail jobDetail = JobBuilder.newJob().ofType(RuleEvaluationJob.class)
                .storeDurably()
                .withIdentity("Qrtz_Job_Detail1")
                .withDescription("Invoke Sample Job service...")
                .build()

        JobDetail jobDetail2 = JobBuilder.newJob().ofType(RuleEvaluationJob.class)
                .storeDurably()
                .withIdentity("Qrtz_Job_Detail2")
                .withDescription("Invoke Sample Job service...")
                .build()

        Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                .withIdentity("Qrtz_Trigger")
                .withDescription("Sample trigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().repeatForever().withIntervalInSeconds(10))
                .build()
        Trigger trigger2 = TriggerBuilder.newTrigger().forJob(jobDetail2)
                .withIdentity("Qrtz_Trigger2")
                .withDescription("Sample trigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(2))
                .build()

//        scheduler.scheduleJob(jobDetail, trigger)
//        scheduler.scheduleJob(jobDetail2, trigger2)

    }

}