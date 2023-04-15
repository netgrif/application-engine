package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfFileListField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfFileListFieldBuilder extends PdfFormFieldBuilder<PdfFileListField> {

    @Override
    public PdfFileListField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfFileListField pdfField = new PdfFileListField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.FILE_LIST.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfFileListField pdfField) {
        FileListField field = (FileListField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String rawValue = field.getValue() != null ? resolveFileListNames(field.getValue().getValue()) : "";
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        List<String> value = generateMultiLineText(Collections.singletonList(rawValue), maxValueLineLength);
        pdfField.setValue(value);
    }

    @Override
    protected int countValueMultiLineHeight(PdfFileListField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    private String resolveFileListNames(FileListFieldValue files) {
        return files.getNamesPaths().stream()
                .map(it -> shortenFileName(it.getName()))
                .collect(Collectors.joining(", "));
    }

    private String shortenFileName(String fileName) {
        if (fileName.length() > 32) {
            return fileName.substring(0, 16) + "..." + fileName.substring(fileName.length() - 8);
        }
        return fileName;
    }


}
