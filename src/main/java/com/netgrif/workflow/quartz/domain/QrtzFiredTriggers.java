package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_FIRED_TRIGGERS", indexes = {
        @Index(name = "IDX_QRTZ_FT_TRIG_INST_NAME", columnList = "SCHED_NAME,INSTANCE_NAME"),
        @Index(name = "IDX_QRTZ_FT_INST_JOB_REQ_RCVRY", columnList = "SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY"),
        @Index(name = "IDX_QRTZ_FT_J_G", columnList = "SCHED_NAME,JOB_NAME,JOB_GROUP"),
        @Index(name = "IDX_QRTZ_FT_JG", columnList = "SCHED_NAME,JOB_GROUP"),
        @Index(name = "IDX_QRTZ_FT_T_G", columnList = "SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP"),
        @Index(name = "IDX_QRTZ_FT_TG", columnList = "SCHED_NAME,TRIGGER_GROUP"),
})
@IdClass(QrtzFiredTriggersPK.class)
public class QrtzFiredTriggers {
    private String schedName;
    private String entryId;
    private String triggerName;
    private String triggerGroup;
    private String instanceName;
    private long firedTime;
    private long schedTime;
    private int priority;
    private String state;
    private String jobName;
    private String jobGroup;
    private String isNonconcurrent;
    private String requestsRecovery;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "ENTRY_ID", length = 95)
    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    @Basic
    @Column(name = "TRIGGER_NAME", length = 190)
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @Basic
    @Column(name = "TRIGGER_GROUP", length = 190)
    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    @Basic
    @Column(name = "INSTANCE_NAME", length = 190)
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Basic
    @Column(name = "FIRED_TIME")
    public long getFiredTime() {
        return firedTime;
    }

    public void setFiredTime(long firedTime) {
        this.firedTime = firedTime;
    }

    @Basic
    @Column(name = "SCHED_TIME")
    public long getSchedTime() {
        return schedTime;
    }

    public void setSchedTime(long schedTime) {
        this.schedTime = schedTime;
    }

    @Basic
    @Column(name = "PRIORITY")
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Basic
    @Column(name = "STATE", length = 16)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Basic
    @Column(name = "JOB_NAME", length = 190)
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Basic
    @Column(name = "JOB_GROUP", length = 190)
    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    @Basic
    @Column(name = "IS_NONCONCURRENT", length = 1)
    public String getIsNonconcurrent() {
        return isNonconcurrent;
    }

    public void setIsNonconcurrent(String isNonconcurrent) {
        this.isNonconcurrent = isNonconcurrent;
    }

    @Basic
    @Column(name = "REQUESTS_RECOVERY", length = 1)
    public String getRequestsRecovery() {
        return requestsRecovery;
    }

    public void setRequestsRecovery(String requestsRecovery) {
        this.requestsRecovery = requestsRecovery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzFiredTriggers that = (QrtzFiredTriggers) o;
        return firedTime == that.firedTime &&
                schedTime == that.schedTime &&
                priority == that.priority &&
                Objects.equals(schedName, that.schedName) &&
                Objects.equals(entryId, that.entryId) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(triggerGroup, that.triggerGroup) &&
                Objects.equals(instanceName, that.instanceName) &&
                Objects.equals(state, that.state) &&
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(jobGroup, that.jobGroup) &&
                Objects.equals(isNonconcurrent, that.isNonconcurrent) &&
                Objects.equals(requestsRecovery, that.requestsRecovery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, entryId, triggerName, triggerGroup, instanceName, firedTime, schedTime, priority, state, jobName, jobGroup, isNonconcurrent, requestsRecovery);
    }
}
