package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Validation implements Serializable {

    private static final long serialVersionUID = 3287600522204188694L;

    protected String name;
    private Arguments arguments;
    private I18nString message;

    @Override
    public Validation clone() {
        Validation cloned =  new Validation();
        cloned.setName(name);
        if (arguments != null) {
            cloned.setArguments(arguments.clone());
        }
        if (this.message != null) {
            cloned.setMessage(this.message.clone());
        }
        return cloned;
    }
}
