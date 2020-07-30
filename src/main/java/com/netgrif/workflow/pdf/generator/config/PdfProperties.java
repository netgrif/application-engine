package com.netgrif.workflow.pdf.generator.config;

import com.netgrif.workflow.pdf.generator.config.types.PdfDateFormat;
import lombok.Data;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@ConfigurationProperties(prefix = "nae.pdf.properties")
@Component
@Data
public class PdfProperties {

   protected int unit = 75;

   protected PDRectangle pageSize = PDRectangle.A4;

   protected int pageWidth = 600;

   protected int pageHeight = 850;

   protected int lineHeight = 20;

   protected int marginTop = unit;

   protected int marginBottom = unit;

   protected int marginLeft = (int) (0.5 * unit);

   protected int marginRight = (int) (0.5 * unit);

   protected int padding = 4;

   protected int pageDrawableWidth = pageWidth - marginLeft - marginRight;

   protected int fontTitleSize = 13;

   protected int fontGroupSize = 13;

   protected int fontLabelSize = 10;

   protected int fontValueSize = 10;

   protected int formGridCols = 4;

   protected int formGridRows = 30;

   protected int formGridColWidth = (pageDrawableWidth / formGridCols);

   protected int formGridRowHeight = ((pageHeight - marginBottom - marginTop) / formGridRows);

   protected float strokeWidth = 0.5f;

   protected boolean textFieldStroke = true;

   protected boolean booleanFieldStroke = false;

   protected float marginMultiplier = 1.65f;

   protected PdfDateFormat dateFormat = PdfDateFormat.SLOVAK1;

   protected Locale numberFormat = new Locale("sk", "SK");

   protected Locale textLocale = new Locale("sk", "SK");

   protected int baseX = marginLeft;

   protected int baseY;
}
