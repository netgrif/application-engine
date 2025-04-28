package com.netgrif.application.engine.petrinet.web.responsebodies;

import lombok.Data;

import java.util.List;

@Data
public class Transaction {

    private List<String> transitions;

    private String title;

    public Transaction() {
    }

    public Transaction(List<String> transitions, String title) {
        this.transitions = transitions;
        this.title = title;
    }
}