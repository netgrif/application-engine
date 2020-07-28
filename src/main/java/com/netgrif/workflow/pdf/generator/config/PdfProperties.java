package com.netgrif.workflow.pdf.generator.config;

import lombok.Data;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nae.pdf.properties")
@Component
@Data
public class PdfProperties {

   protected int ppi = 72;

   protected PDRectangle pageSize = PDRectangle.A4;

   protected int pageWidth = 595;

   protected int pageHeight = 850;

   protected int lineHeight = 18;

   protected int marginTop = ppi;

   protected int marginBottom = ppi;

   protected int marginLeft = (int) (0.5 * ppi);

   protected int marginRight = (int) (0.5 * ppi);

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

   protected int baseX = marginLeft;

   protected int baseY;
}
