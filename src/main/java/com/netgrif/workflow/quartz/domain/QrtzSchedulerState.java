package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_SCHEDULER_STATE")
@IdClass(QrtzSchedulerStatePK.class)
public class QrtzSchedulerState {
    private String schedName;
    private String instanceName;
    private long lastCheckinTime;
    private long checkinInterval;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "INSTANCE_NAME", length = 190)
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Basic
    @Column(name = "LAST_CHECKIN_TIME")
    public long getLastCheckinTime() {
        return lastCheckinTime;
    }

    public void setLastCheckinTime(long lastCheckinTime) {
        this.lastCheckinTime = lastCheckinTime;
    }

    @Basic
    @Column(name = "CHECKIN_INTERVAL")
    public long getCheckinInterval() {
        return checkinInterval;
    }

    public void setCheckinInterval(long checkinInterval) {
        this.checkinInterval = checkinInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzSchedulerState that = (QrtzSchedulerState) o;
        return lastCheckinTime == that.lastCheckinTime &&
                checkinInterval == that.checkinInterval &&
                Objects.equals(schedName, that.schedName) &&
                Objects.equals(instanceName, that.instanceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, instanceName, lastCheckinTime, checkinInterval);
    }
}
