package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_SIMPLE_TRIGGERS")
@IdClass(QrtzSimpleTriggersPK.class)
public class QrtzSimpleTriggers {
    private String schedName;
    private String triggerName;
    private String triggerGroup;
    private long repeatCount;
    private long repeatInterval;
    private long timesTriggered;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "TRIGGER_NAME", length = 190)
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @Id
    @Column(name = "TRIGGER_GROUP", length = 190)
    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    @Basic
    @Column(name = "REPEAT_COUNT")
    public long getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(long repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Basic
    @Column(name = "REPEAT_INTERVAL")
    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Basic
    @Column(name = "TIMES_TRIGGERED")
    public long getTimesTriggered() {
        return timesTriggered;
    }

    public void setTimesTriggered(long timesTriggered) {
        this.timesTriggered = timesTriggered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzSimpleTriggers that = (QrtzSimpleTriggers) o;
        return repeatCount == that.repeatCount &&
                repeatInterval == that.repeatInterval &&
                timesTriggered == that.timesTriggered &&
                Objects.equals(schedName, that.schedName) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(triggerGroup, that.triggerGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, triggerName, triggerGroup, repeatCount, repeatInterval, timesTriggered);
    }
}
