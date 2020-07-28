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

    private File fontTitleFile;

    private File fontLabelFile;

    private File fontValueFile;

    private PDType0Font labelFont;

    private PDType0Font titleFont;

    private PDType0Font valueFont;

    private PDImageXObject checkboxChecked;

    private PDImageXObject checkboxUnchecked;

    private PDImageXObject radioChecked;

    private PDImageXObject radioUnchecked;

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Bold.ttf")
    private Resource fontTitleResource;

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Bold.ttf")
    private Resource fontLabelResource;

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Regular.ttf")
    private Resource fontValueResource;

    @Value("file:src/main/resources/pdfGenerator/img/checkbox_checked.png")
    private Resource checkBoxCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/checkbox_unchecked.png")
    private Resource checkBoxUnCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/radio_checked.png")
    private Resource radioCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/radio_unchecked.png")
    private Resource radioUnCheckedResource;
}
