package com.netgrif.workflow.quartz.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class QrtzSchedulerStatePK implements Serializable {
    private String schedName;
    private String instanceName;

    @Column(name = "SCHED_NAME", length = 120)
    @Id
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Column(name = "INSTANCE_NAME", length = 190)
    @Id
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzSchedulerStatePK that = (QrtzSchedulerStatePK) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(instanceName, that.instanceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, instanceName);
    }
}
