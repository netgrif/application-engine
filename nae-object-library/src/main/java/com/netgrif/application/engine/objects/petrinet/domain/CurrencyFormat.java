package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurrencyFormat extends Format {

    private String code = "EUR";

    private int fractionSize = 2;

    private String locale;
}
