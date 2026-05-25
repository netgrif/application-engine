package com.netgrif.application.engine.adapter.spring.actions;

import java.util.List;

/**
 * Wrapper for list of {@link ProcessAvailability}
 * Use for convenient check of process availability
 *
 * @param availabilities
 */
public record ProcessAvailabilities(List<ProcessAvailability> availabilities) {

    /**
     * Checks if a process with the given identifier is in the "up" state.
     * @param processIdentifier process identifier
     * @return true if a process is up, false otherwise
     */
    public boolean isUp(String processIdentifier) {
        return is(processIdentifier, ProcessAvailability.Status.UP);
    }


    /**
     * Checks if a process with the given identifier is in the "down" state.
     *
     * @param processIdentifier the unique identifier of the process to check
     * @return true if the process is down, false otherwise
     */
    public boolean isDown(String processIdentifier) {
        return is(processIdentifier, ProcessAvailability.Status.DOWN);
    }

    /**
     * Determines if a process with the specified process ID has a status of "not found."
     *
     * @param processIdentifier the identifier of the process to check
     * @return true if the process with the given ID has a status of "not found," false otherwise
     */
    public boolean isNotFound(String processIdentifier) {
        return is(processIdentifier, ProcessAvailability.Status.NOT_FOUND);
    }

    /**
     * Checks if any of the processes in the list are in the "up" status.
     *
     * @return true if at least one process has a status of "up"; false otherwise
     */
    public boolean isAnyUp() {
        return isAny(ProcessAvailability.Status.UP);
    }

    /**
     * Checks if any of the processes in the list are in the "DOWN" status.
     *
     * @return true if at least one process has a status of "DOWN", false otherwise
     */
    public boolean isAnyDown() {
        return isAny(ProcessAvailability.Status.DOWN);
    }

    /**
     * Checks if any process in the list of {@link ProcessAvailability} instances is in the "not found" state.
     *
     * @return true if at least one process has a status of "NOT_FOUND", false otherwise
     */
    public boolean isAnyNotFound() {
        return isAny(ProcessAvailability.Status.NOT_FOUND);
    }

    /**
     * Checks if all processes in the list are in the "UP" state.
     *
     * @return true if all processes are in the "UP" state, false otherwise
     */
    public boolean isAllUp() {
        return isAll(ProcessAvailability.Status.UP);
    }

    /**
     * Checks if all processes in the list are in the "down" state.
     *
     * @return true if all processes are in the "down" state, false otherwise
     */
    public boolean isAllDown() {
        return isAll(ProcessAvailability.Status.DOWN);
    }

    /**
     * Determines if all processes in the list are in the "Not Found" state.
     *
     * @return true if all processes have a status of "Not Found," false otherwise
     */
    public boolean isAllNotFound() {
        return isAll(ProcessAvailability.Status.NOT_FOUND);
    }

    private boolean is(String processIdentifier, ProcessAvailability.Status status) {
        return availabilities.stream()
                .anyMatch(processAvailability -> processAvailability.processIdentifier().equals(processIdentifier) && processAvailability.status() == status);
    }

    private boolean isAny(ProcessAvailability.Status status) {
        return availabilities.stream().anyMatch(processAvailability -> processAvailability.status() == status);
    }

    private boolean isAll(ProcessAvailability.Status status) {
        return availabilities.stream().allMatch(processAvailability -> processAvailability.status() == status);
    }
}
