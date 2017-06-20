package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

/**
 * Transaction object groups multiple transitions together under one title. Transaction only has reference of
 * transitions' ObjectId.
 */
@Document
public class Transaction extends PetriNetObject {

    @Getter @Setter
    private List<ObjectId> transitions;

    @Getter @Setter
    private String title;

    public Transaction() {
        this._id = new ObjectId();
        this.transitions = new LinkedList<>();
    }

    /**
     * Add new transition's ObjectId into this transaction.
     * @param transition
     */
    public void addTransition(Transition transition) {
        transitions.add(transition.getObjectId());
    }
}