package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

/**
 * Transaction object groups multiple transitions together under one title. Transaction only has reference of
 * transitions' ObjectId.
 */
@Getter
public class Transaction extends PetriNetObject {

    @Setter
    private List<String> transitions;

    private I18nString title;

    public Transaction() {
        super();
        this._id = new ObjectId();
        this.transitions = new LinkedList<>();
    }

    public Transaction(Transaction transaction) {
        this.setTransitions(transaction.getTransitions());
        this.setTitle(transaction.getTitle());
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
}
