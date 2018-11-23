package com.netgrif.workflow.petrinet.domain.dataset.format

import com.netgrif.workflow.petrinet.domain.dataset.Format

class CurrencyFormat extends Format {

    String code = "EUR"

    int fractionSize = 2

    String locale
}