package com.netgrif.workflow.rules.domain.scheduled;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.quartz.JobDetail;
import org.quartz.Trigger;

@Data
@AllArgsConstructor
public class ScheduleOutcome {

    private JobDetail jobDetail;
    private Trigger trigger;
}
