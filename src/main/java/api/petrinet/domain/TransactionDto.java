package api.petrinet.domain;

import java.util.List;

public final class TransactionDto extends PetriNetObjectDto {

    private List<String> transitions;

    private I18nStringDto title;

    public TransactionDto() {
    }

    public TransactionDto(String id) {
        super(id);
    }

    public TransactionDto(List<String> transitions, I18nStringDto title) {
        this.transitions = transitions;
        this.title = title;
    }

    public TransactionDto(String id, List<String> transitions, I18nStringDto title) {
        super(id);
        this.transitions = transitions;
        this.title = title;
    }

    public List<String> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<String> transitions) {
        this.transitions = transitions;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }
}
