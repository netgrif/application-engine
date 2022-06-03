package api.workflow.domain;

public final class TaskPairDto {

    private String task;

    private String transition;

    public TaskPairDto() {
    }

    public TaskPairDto(String task, String transition) {
        this.task = task;
        this.transition = transition;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }
}
