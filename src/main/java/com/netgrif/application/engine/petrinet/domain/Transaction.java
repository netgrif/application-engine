package com.netgrif.application.engine.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Transaction object groups multiple transitions together under one title. Transaction only has reference of
 * transitions' ObjectId.
 */
@Document
public class Transaction extends PetriNetObject {

    @Getter
    @Setter
    private List<String> transitions;

    @Getter
    private I18nString title;

    public Transaction() {
        this._id = new ObjectId();
        this.transitions = new LinkedList<>();
    }

    /**
     * Add new transition's ObjectId into this transaction.
     *
     * @param transition
     */
    public void addTransition(Transition transition) {
        transitions.add(transition.getStringId());
    }

    public void setTitle(I18nString title) {
        this.title = title;
    }

    public void setTitle(String title) {
        setTitle(new I18nString(title));
    }

    @Override
    public String toString() {
        return title.getDefaultValue();
    }

    @Override
    public Transaction clone() {
        Transaction clone = new Transaction();
        clone.setTransitions(new ArrayList<>(transitions));
        clone.setTitle(this.title == null ? null : title.clone());
        return clone;
    }
}