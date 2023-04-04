package com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdfTitleFieldBuildingBlock extends PdfBuildingBlock {

    private String text;

    public PdfTitleFieldBuildingBlock() {
        super();
    }

    @Builder
    public PdfTitleFieldBuildingBlock(int lastX, int lastY, Locale locale, String text) {
        super(lastX, lastY, locale);
        this.text = text;
    }
}
