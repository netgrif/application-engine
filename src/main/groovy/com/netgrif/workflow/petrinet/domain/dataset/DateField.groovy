package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document


import java.time.LocalDate
import java.time.ZoneId

@Document
public class DateField extends ValidableField<LocalDate> {

    @Transient
    private String minDate
    @Transient
    private String maxDate

    public DateField() {
        super();
    }

    public void setDefaultValue(String value){
        setDefaultValue(LocalDate.parse(value))
    }

    public void setValue(Date value) {
        this.value = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void convertValue(){
        if(this.value instanceof Date){
            this.value = ((Date)this.value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    String getMinDate() {
        return minDate
    }

    void setMinDate(String minDate) {
        this.minDate = minDate
    }

    String getMaxDate() {
        return maxDate
    }

    void setMaxDate(String maxDate) {
        this.maxDate = maxDate
    }
}
