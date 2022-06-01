package api.petrinet.domain.dataset.logic.action.runner;

public class ExpressionDto {

    protected String id;

    protected String definition;

    ExpressionDto() {
    }

    ExpressionDto(String id, String definition) {
        this();
        this.id = id;
        this.definition = definition;
    }

    public String getId() {
        return this.id;
    }

    public String getDefinition() {
        return definition;
    }

    public String toString() {
        return "[$stringId] $definition";
    }

}
