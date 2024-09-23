package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class Validation implements Serializable {

    private static final long serialVersionUID = 3287600522204188694L;

    private String name;
    private ArrayList<Expression<String>> clientArguments;
    private ArrayList<Expression<String>> serverArguments;
    private I18nString message;

    public Validation() {
        this.clientArguments = new ArrayList<>();
        this.serverArguments = new ArrayList<>();
    }

    @Override
    public Validation clone() {
        Validation cloned = new Validation();
        cloned.setName(name);
        cloned.getClientArguments().addAll(clientArguments);
        cloned.getServerArguments().addAll(serverArguments);
        if (this.message != null) {
            cloned.setMessage(this.message.clone());
        }
        return cloned;
    }
}
