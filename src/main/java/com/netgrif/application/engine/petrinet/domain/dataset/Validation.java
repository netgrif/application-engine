package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Validation implements Serializable {

    private static final long serialVersionUID = 3287600522204188694L;

    protected String name;
    private Arguments clientArguments;
    private Arguments serverArguments;
    private I18nString message;

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
