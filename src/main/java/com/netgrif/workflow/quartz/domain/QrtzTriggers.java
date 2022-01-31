package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_TRIGGERS",
        indexes = {
                @Index(name = "IDX_QRTZ_T_J", columnList = "SCHED_NAME,JOB_NAME,JOB_GROUP"),
                @Index(name = "IDX_QRTZ_T_JG", columnList = "SCHED_NAME,JOB_GROUP"),
                @Index(name = "IDX_QRTZ_T_C", columnList = "SCHED_NAME,CALENDAR_NAME"),
                @Index(name = "IDX_QRTZ_T_G", columnList = "SCHED_NAME,TRIGGER_GROUP"),
                @Index(name = "IDX_QRTZ_T_STATE", columnList = "SCHED_NAME,TRIGGER_STATE"),
                @Index(name = "IDX_QRTZ_T_N_STATE", columnList = "SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE"),
                @Index(name = "IDX_QRTZ_T_N_G_STATE", columnList = "SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE"),
                @Index(name = "IDX_QRTZ_T_NEXT_FIRE_TIME", columnList = "SCHED_NAME,NEXT_FIRE_TIME"),
                @Index(name = "IDX_QRTZ_T_NFT_ST", columnList = "SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME"),
                @Index(name = "IDX_QRTZ_T_NFT_MISFIRE", columnList = "SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME"),
                @Index(name = "IDX_QRTZ_T_NFT_ST_MISFIRE", columnList = "SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE"),
                @Index(name = "IDX_QRTZ_T_NFT_ST_MISFIRE_GRP", columnList = "SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE"),
})
@IdClass(QrtzTriggersPK.class)
public class QrtzTriggers {
    private String schedName;
    private String triggerName;
    private String triggerGroup;
    private String jobName;
    private String jobGroup;
    private String description;
    private Long nextFireTime;
    private Long prevFireTime;
    private Integer priority;
    private String triggerState;
    private String triggerType;
    private long startTime;
    private Long endTime;
    private String calendarName;
    private Short misfireInstr;
    private byte[] jobData;

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
    @Column(name = "DESCRIPTION", length = 250)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "NEXT_FIRE_TIME")
    public Long getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Long nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    @Basic
    @Column(name = "PREV_FIRE_TIME")
    public Long getPrevFireTime() {
        return prevFireTime;
    }

    public void setPrevFireTime(Long prevFireTime) {
        this.prevFireTime = prevFireTime;
    }

    @Basic
    @Column(name = "PRIORITY")
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Basic
    @Column(name = "TRIGGER_STATE", length = 16)
    public String getTriggerState() {
        return triggerState;
    }

    public void setTriggerState(String triggerState) {
        this.triggerState = triggerState;
    }

    @Basic
    @Column(name = "TRIGGER_TYPE", length = 8)
    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    @Basic
    @Column(name = "START_TIME")
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "END_TIME")
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Basic
    @Column(name = "CALENDAR_NAME", length = 190)
    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    @Basic
    @Column(name = "MISFIRE_INSTR")
    public Short getMisfireInstr() {
        return misfireInstr;
    }

    public void setMisfireInstr(Short misfireInstr) {
        this.misfireInstr = misfireInstr;
    }

    @Basic
    @Column(name = "JOB_DATA", length = 65535)
    public byte[] getJobData() {
        return jobData;
    }

    public void setJobData(byte[] jobData) {
        this.jobData = jobData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzTriggers that = (QrtzTriggers) o;
        return startTime == that.startTime &&
                Objects.equals(schedName, that.schedName) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(triggerGroup, that.triggerGroup) &&
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(jobGroup, that.jobGroup) &&
                Objects.equals(description, that.description) &&
                Objects.equals(nextFireTime, that.nextFireTime) &&
                Objects.equals(prevFireTime, that.prevFireTime) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(triggerState, that.triggerState) &&
                Objects.equals(triggerType, that.triggerType) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(calendarName, that.calendarName) &&
                Objects.equals(misfireInstr, that.misfireInstr) &&
                Arrays.equals(jobData, that.jobData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(schedName, triggerName, triggerGroup, jobName, jobGroup, description, nextFireTime, prevFireTime, priority, triggerState, triggerType, startTime, endTime, calendarName, misfireInstr);
        result = 31 * result + Arrays.hashCode(jobData);
        return result;
    }
}
