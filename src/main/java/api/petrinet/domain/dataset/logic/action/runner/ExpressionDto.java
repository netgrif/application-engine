package api.petrinet.domain.dataset.logic.action.runner;

public final class ExpressionDto {

    private String id;

    private String definition;

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
