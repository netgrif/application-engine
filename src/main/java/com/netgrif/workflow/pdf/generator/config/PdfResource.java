package com.netgrif.workflow.pdf.generator.config;

import lombok.Data;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
@ConfigurationProperties(prefix = "nae.pdf.resources")
@Data
public class PdfResource extends PdfProperties {

    private PDType0Font labelFont;

    private PDType0Font titleFont;

    private PDType0Font valueFont;

    private PDImageXObject checkboxChecked;

    private PDImageXObject checkboxUnchecked;

    private PDImageXObject radioChecked;

    private PDImageXObject radioUnchecked;

    private PDImageXObject booleanChecked;

    private PDImageXObject booleanUnchecked;

    private Resource fontTitleResource;

    private Resource fontLabelResource;

    private Resource fontValueResource;

    private Resource outputResource;

    private Resource templateResource;

    private Resource checkBoxCheckedResource;

    private Resource checkBoxUnCheckedResource;

    private Resource radioCheckedResource;

    private Resource radioUnCheckedResource;

    private Resource booleanCheckedResource;

    private Resource booleanUncheckedResource;
}
