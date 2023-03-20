package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Data
@AllArgsConstructor
@Builder
public class PdfFieldBuildingBlock {

    private DataGroup dataGroup;

    private DataRef dataRef;

    private int lastX;

    private int lastY;

    private Locale locale;

    public PdfFieldBuildingBlock() {
        this.locale = LocaleContextHolder.getLocale();
    }
}
