package api.petrinet.domain;

public class FunctionDto extends PetriNetObjectDto {

    private String definition;

    private String name;

    private String scope;

    public FunctionDto() {
    }

    public FunctionDto(String id) {
        super(id);
    }

    public FunctionDto(String definition, String name, String scope) {
        this.definition = definition;
        this.name = name;
        this.scope = scope;
    }

    public FunctionDto(String id, String definition, String name, String scope) {
        super(id);
        this.definition = definition;
        this.name = name;
        this.scope = scope;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
