package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfMultiChoiceFieldRenderer extends PdfFieldRenderer<Map<String, List<String>>, PdfMultiChoiceField> {

    @Override
    public String getType() {
        return DataType.MULTICHOICE.value();
    }

    @Override
    public void renderValue() throws IOException {
//        int maxLineSize = getMaxValueLineSize(field.getWidth() - 3 * padding);
//        List<String> multiLineText;
        PdfMultiChoiceField clonedField = (PdfMultiChoiceField) getField().getCopier().copyOf();
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + 4 * getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);

        for (Map.Entry<String, List<String>> choice : clonedField.getValue().entrySet()) {
            boolean buttonDrawn = false;
//            float textWidth = getTextWidth(choice.getValue(), clonedField.getValueFont(), fontValueSize, resource);
//            multiLineText = new ArrayList<>() {{
//                add(choice);
//            }};

//            if (textWidth > field.getWidth() - 4 * padding) {
//                multiLineText = PdfFieldBuilder.generateMultiLineText(Collections.singletonList(choice), maxLineSize);
//            }

            for (String line : choice.getValue()) {
                lineCounter++;
                lineCounter = renderPageBrake(clonedField, lineCounter, y);
                y = renderLinePosY(clonedField, lineCounter);
                getPdfDrawer().writeString(getResource().getValueFont(), getResource().getFontValueSize(), x, y, line, Color.decode(getResource().getColorString().toUpperCase()));
                if (!buttonDrawn) {
                    buttonDrawn = getPdfDrawer().drawSelectionButton(clonedField.getSelectedValues(), choice.getKey(), clonedField.getX() + getResource().getPadding(), y, clonedField.getType());
                }
            }
        }
        getPdfDrawer().checkOpenPages();
    }

//    public void setFieldParams(PdfMultiChoiceField field) {
//        helperField = new PdfMultiChoiceField(field.getFieldId(), field.getLabel(), field.getValue(), field.getChoices(), field.getType(), resource.getBaseX() + field.getX(),
//                resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
//    }

//    @Override
//    public int renderLabel(PdfField field) throws IOException {
//        setFieldParams((PdfMultiChoiceField) field);
//        return renderLabel(helperField, resource.getLabelFont(), fontLabelSize, colorLabelString);
//    }

//    public void renderValue(PdfField field, int lineCounter) throws IOException {
//        setFieldParams((PdfMultiChoiceField) field);
//        renderValue((PdfSelectionField) helperField, lineCounter);
//    }
}
