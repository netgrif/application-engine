package com.netgrif.application.engine.export.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;

import java.util.function.BiConsumer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellType {

    /**
     * @see org.apache.poi.ss.usermodel.BuiltinFormats
     */
    private short format;
    private BiConsumer<Cell, Object> cellValueSetter;

}
