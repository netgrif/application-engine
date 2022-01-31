package com.netgrif.workflow.quartz.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class QrtzLocksPK implements Serializable {
    private String schedName;
    private String lockName;

    @Column(name = "SCHED_NAME", length = 120)
    @Id
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Column(name = "LOCK_NAME")
    @Id
    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzLocksPK that = (QrtzLocksPK) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(lockName, that.lockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, lockName);
    }
}
