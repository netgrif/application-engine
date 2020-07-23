package com.netgrif.workflow.pdf.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class PdfResources extends PdfProperties {
    @Getter
    @Setter
    public File fontTitleFile;

    @Getter
    @Setter
    public File fontLabelFile;

    @Getter
    @Setter
    public File fontValueFile;

    @Getter
    @Setter
    public static PDType0Font labelFont;

    @Getter
    @Setter
    public static PDType0Font titleFont;

    @Getter
    @Setter
    public static PDType0Font valueFont;

    @Getter
    @Setter
    public static PDImageXObject checkboxChecked;

    @Getter
    @Setter
    public static PDImageXObject checkboxUnchecked;

    @Getter
    @Setter
    public static PDImageXObject radioChecked;

    @Getter
    @Setter
    public static PDImageXObject radioUnchecked;

    @Getter
    @Setter
    public static Color colorBorder = new Color(204, 204, 204);

    @Getter
    @Setter
    public static Color whiteColorBorder = new Color(255, 255, 255);

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Bold.ttf")
    public Resource fontTitleResource;

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Bold.ttf")
    public Resource fontLabelResource;

    @Value("file:src/main/resources/pdfGenerator/fonts/SpaceMono-Regular.ttf")
    public Resource fontValueResource;

    @Value("file:src/main/resources/pdfGenerator/img/checkbox_checked.png")
    public Resource checkBoxCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/checkbox_unchecked.png")
    public Resource checkBoxUnCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/radio_checked.png")
    public Resource radioCheckedResource;

    @Value("file:src/main/resources/pdfGenerator/img/radio_unchecked.png")
    public Resource radioUnCheckedResource;
}
