package com.netgrif.workflow.pdf.generator.service

import lombok.Getter
import lombok.Setter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

import java.awt.Color

class PDFGeneratorProperties {

   @Getter
   @Setter
   int ppi = 72

   @Getter
   @Setter
   PDRectangle pageSize = PDRectangle.A4

   @Getter
   @Setter
   int pageWidth = 595

   @Getter
   @Setter
   int pageHeight = 850

   @Getter
   @Setter
   int lineHeight = 18

   @Getter
   @Setter
   int marginTop = 1 * ppi

   @Getter
   @Setter
   int marginBottom = 1 * ppi

   @Getter
   @Setter
   int marginLeft = (int) (0.5 * ppi)

   @Getter
   @Setter
   int marginRight = (int) (0.5 * ppi)

   @Getter
   @Setter
   int padding = 5

   @Getter
   @Setter
   int pageDrawableWidth = pageWidth - marginLeft - marginRight

   @Getter
   @Setter
   int fontTitleSize = 18

   @Getter
   @Setter
   int fontValueSize = 10

   @Getter
   @Setter
   int fontLabelSize = 13

   @Getter
   @Setter
   int fontGroupSize = 16

   @Getter
   @Setter
   int actualCharactersInRow = 85

   @Getter
   @Setter
   int shortCharactersInRow = 80

   @Getter
   @Setter
   File fontTitleFile

   @Getter
   @Setter
   File fontLabelFile

   @Getter
   @Setter
   File fontValueFile

   @Getter
   @Setter
   PDType0Font labelFont

   @Getter
   @Setter
   PDType0Font titleFont

   @Getter
   @Setter
   PDType0Font valueFont

   @Getter
   @Setter
   PDImageXObject checkboxChecked

   @Getter
   @Setter
   PDImageXObject checkboxUnchecked

   @Getter
   @Setter
   PDImageXObject radioChecked

   @Getter
   @Setter
   PDImageXObject radioUnchecked

   @Getter
   @Setter
   Color colorBorder = new Color(204, 204, 204)

   @Getter
   @Setter
   Color whiteColorBorder = new Color(255, 255, 255)

   @Getter
   @Setter
   String fontTitlePath = "src/main/resources/pdfGenerator/fonts/OpenSansCondensed-Bold.ttf"

   @Getter
   @Setter
   String fontLabelPath = "src/main/resources/pdfGenerator/fonts/OpenSansCondensed-Light.ttf"

   @Getter
   @Setter
   String fontValuePath = "src/main/resources/pdfGenerator/fonts/SourceCodePro-Light.ttf"

   @Getter
   @Setter
   String checkBoxCheckedPath = "src/main/resources/pdfGenerator/img/checkbox_checked.png"

   @Getter
   @Setter
   String checkBoxUnCheckedPath = "src/main/resources/pdfGenerator/img/checkbox_unchecked.png"

   @Getter
   @Setter
   String radioCheckedPath = "src/main/resources/pdfGenerator/img/radio_checked.png"

   @Getter
   @Setter
   String radioUnCheckedPath = "src/main/resources/pdfGenerator/img/radio_unchecked.png"
}
