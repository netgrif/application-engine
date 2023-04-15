package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfUserListField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import org.jsoup.Jsoup;
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
public class PdfUserListFieldBuilder extends PdfFormFieldBuilder<PdfUserListField> {

    @Override
    public PdfUserListField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfUserListField pdfField = new PdfUserListField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.USER_LIST.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfUserListField pdfField) {
        UserListField field = (UserListField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String rawValue = field.getValue() != null ? field.getValue().getValue().getUserValues().stream().map(UserFieldValue::getFullName).collect(Collectors.joining(", ")) : "";
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
    protected int countValueMultiLineHeight(PdfUserListField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }
}
