package com.netgrif.workflow.quartz.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class QrtzCalendarsPK implements Serializable {
    private String schedName;
    private String calendarName;

    @Column(name = "SCHED_NAME", length = 120)
    @Id
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Column(name = "CALENDAR_NAME", length = 190)
    @Id
    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzCalendarsPK that = (QrtzCalendarsPK) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(calendarName, that.calendarName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, calendarName);
    }
}
