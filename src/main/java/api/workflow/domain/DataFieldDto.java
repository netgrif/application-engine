package api.workflow.domain;

import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import api.petrinet.domain.I18nStringDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DataFieldDto {

    private Map<String, Set<String>> behavior;

    private Object value;

    private Set<I18nStringDto> choices;

    private List<String> allowedNets;

    private Map<String, I18nStringDto> options;

    private List<ValidationDto> validations;

    private Map<String, Object> filterMetadata;

    private String encryption;

    private Long version;

    public DataFieldDto() {
    }

    public DataFieldDto(Map<String, Set<String>> behavior, Object value, Set<I18nStringDto> choices, List<String> allowedNets, Map<String, I18nStringDto> options, List<ValidationDto> validations, Map<String, Object> filterMetadata, String encryption, Long version) {
        this.behavior = behavior;
        this.value = value;
        this.choices = choices;
        this.allowedNets = allowedNets;
        this.options = options;
        this.validations = validations;
        this.filterMetadata = filterMetadata;
        this.encryption = encryption;
        this.version = version;
    }

    public Map<String, Set<String>> getBehavior() {
        return behavior;
    }

    public void setBehavior(Map<String, Set<String>> behavior) {
        this.behavior = behavior;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Set<I18nStringDto> getChoices() {
        return choices;
    }

    public void setChoices(Set<I18nStringDto> choices) {
        this.choices = choices;
    }

    public List<String> getAllowedNets() {
        return allowedNets;
    }

    public void setAllowedNets(List<String> allowedNets) {
        this.allowedNets = allowedNets;
    }

    public Map<String, I18nStringDto> getOptions() {
        return options;
    }

    public void setOptions(Map<String, I18nStringDto> options) {
        this.options = options;
    }

    public List<ValidationDto> getValidations() {
        return validations;
    }

    public void setValidations(List<ValidationDto> validations) {
        this.validations = validations;
    }

    public Map<String, Object> getFilterMetadata() {
        return filterMetadata;
    }

    public void setFilterMetadata(Map<String, Object> filterMetadata) {
        this.filterMetadata = filterMetadata;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
