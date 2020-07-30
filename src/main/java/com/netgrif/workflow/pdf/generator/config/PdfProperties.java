package com.netgrif.workflow.pdf.generator.config;

import com.netgrif.workflow.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.workflow.pdf.generator.config.types.PdfDateFormat;
import com.netgrif.workflow.pdf.generator.config.types.PdfPageNumberFormat;
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

   protected int marginTitle = (int) (0.5 * unit);

   protected int marginTop = unit;

   protected int marginBottom = unit;

   protected int marginLeft = (int) (0.5 * unit);

   protected int marginRight = (int) (0.5 * unit);

   protected int padding = 4;

   protected int boxPadding = 2;

   protected int baseX = marginLeft;

   protected int baseY;

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

   protected int boxSize = 10;

   protected float sizeMultiplier = 1.65f;

   protected boolean textFieldStroke = true;

   protected boolean booleanFieldStroke = false;

   protected PdfDateFormat dateFormat = PdfDateFormat.SLOVAK1;

   protected Locale numberFormat = new Locale("sk", "SK");

   protected Locale textLocale = new Locale("sk", "SK");

   protected String documentTitle;

   protected PdfBooleanFormat booleanFormat = PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_SK;

   protected PdfPageNumberFormat pageNumberFormat = PdfPageNumberFormat.SLASH;

   protected int pageNumberPosition = (int) (0.5 * pageWidth);

   public void resetProperties(){
      pageDrawableWidth = pageWidth - marginLeft - marginRight;
      formGridColWidth = (pageDrawableWidth / formGridCols);
      formGridRowHeight = ((pageHeight - marginBottom - marginTop) / formGridRows);
      baseX = marginLeft;
   }

   public void resetProperties(int unit){
      marginTop = unit;
      marginBottom = unit;
      marginLeft = (int) (0.5 * unit);
      marginRight = (int) (0.5 * unit);
      marginTitle = (int) (0.5 * unit);
      pageDrawableWidth = pageWidth - marginLeft - marginRight;
      formGridColWidth = (pageDrawableWidth / formGridCols);
      formGridRowHeight = ((pageHeight - marginBottom - marginTop) / formGridRows);
      baseX = marginLeft;
   }
}
