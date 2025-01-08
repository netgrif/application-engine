package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.workflow.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Validation implements Serializable {

    private static final long serialVersionUID = 3287600522204188694L;

    protected String name;
    private Arguments clientArguments;
    private Arguments serverArguments;
    private I18nString message;

    public Validation() {
        this.clientArguments = new Arguments();
        this.serverArguments = new Arguments();
    }

    @Override
    public Validation clone() {
        Validation cloned =  new Validation();
        cloned.setName(name);
        if (clientArguments != null) {
            cloned.setClientArguments(clientArguments.clone());
        }
        if (serverArguments != null) {
            cloned.setServerArguments(serverArguments.clone());
        }
        if (this.message != null) {
            cloned.setMessage(this.message.clone());
        }
        return cloned;
    }
}
