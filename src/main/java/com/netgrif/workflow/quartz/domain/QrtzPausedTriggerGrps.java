package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_PAUSED_TRIGGER_GRPS")
@IdClass(QrtzPausedTriggerGrpsPK.class)
public class QrtzPausedTriggerGrps {
    private String schedName;
    private String triggerGroup;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "TRIGGER_GROUP", length = 190)
    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzPausedTriggerGrps that = (QrtzPausedTriggerGrps) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(triggerGroup, that.triggerGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, triggerGroup);
    }
}
