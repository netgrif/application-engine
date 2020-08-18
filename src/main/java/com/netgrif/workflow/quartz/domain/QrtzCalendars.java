package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_CALENDARS")
@IdClass(QrtzCalendarsPK.class)
public class QrtzCalendars {
    private String schedName;
    private String calendarName;
    private byte[] calendar;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "CALENDAR_NAME", length = 190)
    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    @Basic
    @Column(name = "CALENDAR", length = 65535)
    public byte[] getCalendar() {
        return calendar;
    }

    public void setCalendar(byte[] calendar) {
        this.calendar = calendar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzCalendars that = (QrtzCalendars) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(calendarName, that.calendarName) &&
                Arrays.equals(calendar, that.calendar);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(schedName, calendarName);
        result = 31 * result + Arrays.hashCode(calendar);
        return result;
    }
}
