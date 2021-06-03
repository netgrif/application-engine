package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised.LocalisedSetDataChangedFieldsEventOutcome;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class SetDataChangedFieldsEventOutcome extends EventOutcome {

    private ChangedFieldContainer data;

    public SetDataChangedFieldsEventOutcome() {
    }

    public SetDataChangedFieldsEventOutcome(I18nString message, List<EventOutcome> outcomes, ChangedFieldContainer data) {
        super(message, outcomes);
        this.data = data;
    }

    public SetDataChangedFieldsEventOutcome(I18nString message, ChangedFieldContainer data) {
        super(message);
        this.data = data;
    }

    public SetDataChangedFieldsEventOutcome(ChangedFieldContainer data) {
        this.data = data;
    }

    public SetDataChangedFieldsEventOutcome(SetDataEventOutcome setDataEventOutcome) {
        super(setDataEventOutcome.getMessage(), setDataEventOutcome.getOutcomes());
        this.data = setDataEventOutcome.getData().flatten();
    }

    @Override
    public LocalisedSetDataChangedFieldsEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedSetDataChangedFieldsEventOutcome(this, locale);
    }
}
