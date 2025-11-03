package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.TransactionDto;
import com.netgrif.application.engine.objects.petrinet.domain.Transaction;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionResource extends EntityModel<TransactionDto> {

    public TransactionResource(Transaction content, Locale locale) {
        super(new TransactionDto(content.getTransitions(), content.getTitle().getTranslation(locale)), new ArrayList<>());
    }
}
