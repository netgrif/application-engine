package com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks;

import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Data
public abstract class PdfBuildingBlock {

    private int lastX;

    private int lastY;

    private Locale locale;

    public PdfBuildingBlock() {
        this.locale = LocaleContextHolder.getLocale();
    }

    public PdfBuildingBlock(int lastX, int lastY, Locale locale) {
        this.lastX = lastX;
        this.lastY = lastY;
        this.locale = locale;
    }
}
