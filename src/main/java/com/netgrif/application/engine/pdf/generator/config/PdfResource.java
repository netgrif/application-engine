package com.netgrif.application.engine.pdf.generator.config;

import lombok.Data;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "nae.pdf.resources")
@Data
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfResource extends PdfProperties {

    private PDType0Font labelFont;

    private PDType0Font titleFont;

    private PDType0Font valueFont;

    private PDFormXObject checkboxChecked;

    private PDFormXObject checkboxUnchecked;

    private PDFormXObject radioChecked;

    private PDFormXObject radioUnchecked;

    private PDFormXObject booleanChecked;

    private PDFormXObject booleanUnchecked;

    private Resource fontTitleResource;

    private Resource fontLabelResource;

    private Resource fontValueResource;

    private String outputFolder;

    private String outputDefaultName;

    private Resource outputResource;

    private Resource templateResource;

    private Resource checkBoxCheckedResource;

    private Resource checkBoxUnCheckedResource;

    private Resource radioCheckedResource;

    private Resource radioUnCheckedResource;

    private Resource booleanCheckedResource;

    private Resource booleanUncheckedResource;
}
