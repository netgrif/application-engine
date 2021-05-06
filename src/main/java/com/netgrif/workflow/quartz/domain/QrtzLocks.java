package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_LOCKS")
@IdClass(QrtzLocksPK.class)
public class QrtzLocks {
    private String schedName;
    private String lockName;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "LOCK_NAME", length = 40)
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
        QrtzLocks qrtzLocks = (QrtzLocks) o;
        return Objects.equals(schedName, qrtzLocks.schedName) &&
                Objects.equals(lockName, qrtzLocks.lockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, lockName);
    }
}
