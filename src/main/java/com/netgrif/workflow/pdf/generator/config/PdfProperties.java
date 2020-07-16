package com.netgrif.workflow.pdf.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public abstract class PdfProperties {

   public static int PPI = 72;

   public PDRectangle pageSize = PDRectangle.A4;

   public static int GRID_WIDTH = 4;

   public static int PAGE_WIDTH = 595;

   public static int PAGE_HEIGHT = 850;

   public static int LINE_HEIGHT = 18;

   public static int MARGIN_TOP = PPI;

   public static int MARGIN_BOTTOM = PPI;

   public static int MARGIN_LEFT = (int) (0.5 * PPI);

   public static int MARGIN_RIGHT = (int) (0.5 * PPI);

   public static int PADDING = 5;

   public static int PAGE_DRAWABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

   public static int FONT_TITLE_SIZE = 10;

   public static int FONT_GROUP_SIZE = 12;

   public static int FONT_LABEL_SIZE = 10;

   public static int FONT_VALUE_SIZE = 10;

   public static final int FORM_GRID_COLS = 4;

   public static final int FORM_GRID_ROWS = 25;

   public static final int FORM_GRID_COL_WIDTH = (PAGE_DRAWABLE_WIDTH / FORM_GRID_COLS);

   public static final int FORM_GRID_ROW_HEIGHT = ((PAGE_HEIGHT - MARGIN_BOTTOM - MARGIN_TOP) / FORM_GRID_ROWS);

   public static int BASE_X = MARGIN_LEFT;

   public static int BASE_Y = PAGE_HEIGHT - MARGIN_TOP;
}
