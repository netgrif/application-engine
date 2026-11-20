package com.netgrif.application.engine.petrinet.domain.dataset


import com.netgrif.application.engine.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationMapField extends MapOptionsField<I18nString, String> {

    EnumerationMapField() {
        super()
    }

    EnumerationMapField(Map<String, I18nString> options) {
        super(options)
    }

    EnumerationMapField(Map<String, I18nString> options, String defaultValue) {
        super(options)
        this.defaultValue = defaultValue
    }

    @Override
    FieldType getType() {
        return FieldType.ENUMERATION_MAP
    }

    @Override
    Map<String, I18nString> getOptions() {
        return super.getOptions()
    }

    @Override
    void setOptions(Map<String, I18nString> options) {
        super.setOptions(options)
    }

    @Override
    String getDefaultValue() {
        return super.getDefaultValue()
    }

    @Override
    void setDefaultValue(String defaultValue) {
        super.setDefaultValue(defaultValue)
    }

    /**
     * Returns the internationalized string value corresponding to the currently selected option key.
     * <p>
     * This method retrieves the {@link I18nString} from the options map that corresponds to the
     * current value of this field.
     * </p>
     *
     * @return the {@link I18nString} object representing the internationalized value of the selected
     *         option, or {@code null} if the field's value is null or if no matching option exists.
     */
    I18nString getI18nValue() {
        if (this.getValue() == null) {
            return null;
        }
        return this.options?.get(this.getValue())
    }

    @Override
    Field clone() {
        EnumerationMapField clone = new EnumerationMapField()
        super.clone(clone)
        clone.options = options
        clone.optionsExpression = optionsExpression
        return clone
    }
}
