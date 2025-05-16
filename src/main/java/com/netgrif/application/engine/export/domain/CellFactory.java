package com.netgrif.application.engine.export.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;

import java.util.Map;

import static com.netgrif.application.engine.export.utils.XlsExportDateUtils.*;

public class CellFactory {

    public static String DATE_PATTERN = "yyyy-MM-dd";
    public static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Map<FieldType, CellType> CELL_TYPE_MAP = Map.of(
            FieldType.BOOLEAN, new CellType((short) 0x0, (cell, value) -> cell.setCellValue(value.equals(true))),
            FieldType.NUMBER, new CellType((short) 0x2, (cell, value) -> cell.setCellValue(Math.round(Double.parseDouble(value.toString()) * 100.0) / 100.0)),
            FieldType.DATE, new CellType((short) 0xe, (cell, value) -> cell.setCellValue(dateToString(convertToLocalDateIfNeeded(value), DATE_PATTERN))),
            FieldType.DATETIME, new CellType((short) 0x16, (cell, value) -> cell.setCellValue(dateTimeToString(convertToLocalDateTimeIfNeeded(value), DATE_TIME_PATTERN))),
            FieldType.USER, new CellType((short) 0x0, (cell, value) -> cell.setCellValue(((UserFieldValue) value).getFullName()))
    );
    private static final CellType DEFAULT_CELL_TYPE = new CellType((short) 0x0, (cell, value) -> cell.setCellValue(value.toString()));

    public static Cell create(Row row, int columnIndex, Field<?> field) {
        return create(row, columnIndex, field.getType(), field.getValue());
    }

    public static Cell create(Row row, int columnIndex, FieldType fieldType, Object value) {
        CellType cellType = CELL_TYPE_MAP.getOrDefault(fieldType, DEFAULT_CELL_TYPE);
        Cell cell = row.createCell(columnIndex);
        CellUtil.setCellStyleProperty(cell, CellUtil.DATA_FORMAT, cellType.getFormat());
        if (value != null)
            cellType.getCellValueSetter().accept(cell, value);
        return cell;
    }

}
