package com.netgrif.workflow.petrinet.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName("transaction")
public class LocalisedTransaction {

    private List<String> transitions;

    private String title;

    public LocalisedTransaction() {
    }

    public LocalisedTransaction(List<String> transitions, String title) {
        this.transitions = transitions;
        this.title = title;
    }
}