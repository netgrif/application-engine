package com.netgrif.workflow.petrinet.domain.arcs.reference;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reference {

    private String reference;

    private Type type;
}
