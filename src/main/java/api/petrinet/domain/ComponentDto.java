package api.petrinet.domain;

import java.util.List;
import java.util.Map;

public final class ComponentDto {

    private String name;

    private Map<String, String> properties;

    private List<IconDto> optionIcons;

    public ComponentDto() {
    }

    public ComponentDto(String name, Map<String, String> properties, List<IconDto> optionIcons) {
        this.name = name;
        this.properties = properties;
        this.optionIcons = optionIcons;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<IconDto> getOptionIcons() {
        return optionIcons;
    }

    public void setOptionIcons(List<IconDto> optionIcons) {
        this.optionIcons = optionIcons;
    }
}
