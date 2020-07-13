package com.netgrif.workflow.pdf.generator.data

import com.netgrif.workflow.petrinet.domain.DataGroup
import com.netgrif.workflow.workflow.domain.DataField
import lombok.Getter
import lombok.Setter

class TransitionFormData {

    @Getter
    @Setter
    int gridWidth

    @Getter
    @Setter
    Map<String, DataGroup> dataGroups

    @Getter
    @Setter
    Map<String, DataField> dataSet

    TransitionFormData(Map<String, DataGroup> dataGroups, Map<String, DataField> dataSet, int gridWidth = 4){
        this.dataGroups = dataGroups
        this.dataSet = dataSet
        this.gridWidth = gridWidth
    }
}
