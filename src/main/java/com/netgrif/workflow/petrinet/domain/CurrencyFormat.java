package com.netgrif.workflow.petrinet.domain;

import lombok.Data;

@Data
public class CurrencyFormat extends Format {

    private String code = "EUR";

    private int fractionSize = 2;

    private String locale;
}