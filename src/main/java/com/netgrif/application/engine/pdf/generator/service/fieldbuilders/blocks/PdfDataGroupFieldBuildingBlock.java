package com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdfDataGroupFieldBuildingBlock extends PdfBuildingBlock {

    private int y;

    private String importId;

    private I18nString title;

    @Builder
    public PdfDataGroupFieldBuildingBlock(int lastX, int lastY, Locale locale, int y, String importId, I18nString title) {
        super(lastX, lastY, locale);
        this.y = y;
        this.importId = importId;
        this.title = title;
    }
}
