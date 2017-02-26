package com.fmworkflow.workflow.domain;

import com.fmworkflow.auth.domain.User;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String caseId;
    private String transitionId;
    private String title;
    private String caseColor;
    private String visualId;
    private int priority;
    @ManyToOne
    private User user;
    private String assignRole;
    private DateTime startDate;
    private DateTime finishDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaseColor() {
        return caseColor;
    }

    public void setCaseColor(String caseColor) {
        this.caseColor = caseColor == null || caseColor.isEmpty() ? "color-fg-indigo-500" : caseColor;
    }

    public String getVisualId() {
        return visualId;
    }

    public void setVisualId(String petriNetInitials) {
        this.visualId = petriNetInitials+"-"+this.id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority == Priorities.UNDEFINED ? Priorities.LOW : priority;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(DateTime finishDate) {
        this.finishDate = finishDate;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }

    public String getAssignRole() {
        return assignRole;
    }

    public void setAssignRole(String assignRole) {
        this.assignRole = assignRole;
    }

    public static class Priorities {
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
        public static final int UNDEFINED = 0;

    }
}