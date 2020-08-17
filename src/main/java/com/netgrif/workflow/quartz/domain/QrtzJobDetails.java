package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_JOB_DETAILS",
       indexes = {
        @Index(name = "IDX_QRTZ_J_REQ_RECOVERY", columnList = "SCHED_NAME,REQUESTS_RECOVERY"),
        @Index(name = "IDX_QRTZ_J_GRP", columnList = "SCHED_NAME,JOB_GROUP"),
})
@IdClass(QrtzJobDetailsPK.class)
public class QrtzJobDetails {
    private String schedName;
    private String jobName;
    private String jobGroup;
    private String description;
    private String jobClassName;
    private String isDurable;
    private String isNonconcurrent;
    private String isUpdateData;
    private String requestsRecovery;
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
    @Column(name = "JOB_NAME", length = 190)
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Id
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
    @Column(name = "JOB_CLASS_NAME", length = 250)
    public String getJobClassName() {
        return jobClassName;
    }

    public void setJobClassName(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    @Basic
    @Column(name = "IS_DURABLE", length = 1)
    public String getIsDurable() {
        return isDurable;
    }

    public void setIsDurable(String isDurable) {
        this.isDurable = isDurable;
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
    @Column(name = "IS_UPDATE_DATA", length = 1)
    public String getIsUpdateData() {
        return isUpdateData;
    }

    public void setIsUpdateData(String isUpdateData) {
        this.isUpdateData = isUpdateData;
    }

    @Basic
    @Column(name = "REQUESTS_RECOVERY", length = 1)
    public String getRequestsRecovery() {
        return requestsRecovery;
    }

    public void setRequestsRecovery(String requestsRecovery) {
        this.requestsRecovery = requestsRecovery;
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
        QrtzJobDetails that = (QrtzJobDetails) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(jobGroup, that.jobGroup) &&
                Objects.equals(description, that.description) &&
                Objects.equals(jobClassName, that.jobClassName) &&
                Objects.equals(isDurable, that.isDurable) &&
                Objects.equals(isNonconcurrent, that.isNonconcurrent) &&
                Objects.equals(isUpdateData, that.isUpdateData) &&
                Objects.equals(requestsRecovery, that.requestsRecovery) &&
                Arrays.equals(jobData, that.jobData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(schedName, jobName, jobGroup, description, jobClassName, isDurable, isNonconcurrent, isUpdateData, requestsRecovery);
        result = 31 * result + Arrays.hashCode(jobData);
        return result;
    }
}
