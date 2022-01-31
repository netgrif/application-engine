package com.netgrif.workflow.quartz.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class QrtzTriggersPK implements Serializable {
    private String schedName;
    private String triggerName;
    private String triggerGroup;

    @Column(name = "SCHED_NAME", length = 120)
    @Id
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Column(name = "TRIGGER_NAME", length = 190)
    @Id
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @Column(name = "TRIGGER_GROUP", length = 190)
    @Id
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
        QrtzTriggersPK that = (QrtzTriggersPK) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(triggerGroup, that.triggerGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, triggerName, triggerGroup);
    }
}
