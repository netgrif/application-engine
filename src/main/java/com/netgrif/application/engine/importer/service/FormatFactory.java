package com.netgrif.application.engine.importer.service;

import com.netgrif.core.petrinet.domain.CurrencyFormat;
import com.netgrif.core.petrinet.domain.Format;
import org.springframework.stereotype.Component;

@Component
public class FormatFactory {

    public Format buildFormat(com.netgrif.core.importer.model.Format format) {
        if (format.getCurrency() != null) {
            CurrencyFormat result = new CurrencyFormat();
            result.setCode(format.getCurrency().getCode());
            result.setFractionSize(format.getCurrency().getFractionSize());
            result.setLocale(format.getCurrency().getLocale());
            return result;
        } else {
            throw new IllegalArgumentException("Unsupported format exception");
        }
    }
}