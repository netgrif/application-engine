package com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks;

import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdfFormFieldBuildingBlock extends PdfBuildingBlock {

    private DataGroup dataGroup;

    private DataRef dataRef;

    public PdfFormFieldBuildingBlock(DataGroup dataGroup, DataRef dataRef) {
        this.dataGroup = dataGroup;
        this.dataRef = dataRef;
    }

    @Builder
    public PdfFormFieldBuildingBlock(int lastX, int lastY, Locale locale, DataGroup dataGroup, DataRef dataRef) {
        super(lastX, lastY, locale);
        this.dataGroup = dataGroup;
        this.dataRef = dataRef;
    }
}
