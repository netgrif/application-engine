package com.fmworkflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fmworkflow.auth.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Document
public class Task {
    @Id
    private ObjectId _id;
    private String caseId;
    private String transitionId;
    private String title;
    private String caseColor;
    private String caseTitle;
    private String visualId;
    private int priority;
    private Long userId;
    @DBRef
    private List<Trigger> triggers;
    private String assignRole;
    private String delegateRole;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    public Task() {
        this._id = new ObjectId();
        this.triggers = new LinkedList<>();
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId id) {
        this._id = id;
    }

    public String getStringId() {
        return _id.toString();
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
        this.caseColor = caseColor;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public String getVisualId() {
        return visualId;
    }

    public void setVisualId(String petriNetInitials) {
        // TODO: 9.5.2017 bullshit
        this.visualId = petriNetInitials+"-"+this._id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority == Priorities.UNDEFINED ? Priorities.LOW : priority;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @JsonIgnore
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

    public String getDelegateRole() {
        return delegateRole;
    }

    public void setDelegateRole(String delegateRole) {
        this.delegateRole = delegateRole;
    }

    @JsonIgnore
    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public static class Priorities {
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
        public static final int UNDEFINED = 0;

    }

    public enum Type {
        USER,
        AUTO,
        TIME,
        MESSAGE,
    }
}