package com.netgrif.workflow.quartz.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class QrtzJobDetailsPK implements Serializable {
    private String schedName;
    private String jobName;
    private String jobGroup;

    @Column(name = "SCHED_NAME", length = 120)
    @Id
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Column(name = "JOB_NAME", length = 190)
    @Id
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "JOB_GROUP", length = 190)
    @Id
    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzJobDetailsPK that = (QrtzJobDetailsPK) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(jobGroup, that.jobGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, jobName, jobGroup);
    }
}
