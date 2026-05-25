package com.netgrif.application.engine.adapter.spring.actions;

/**
 * Represents the availability status of a process.
 * This class encapsulates information about a process's identifier and its status.
 * It provides utility methods to interpret and construct instances based on different inputs.
 */
public record ProcessAvailability(String processIdentifier, Status status) {

    public static ProcessAvailability from(String processIdentifier, Boolean status) {
        return new ProcessAvailability(processIdentifier, Status.from(status));
    }

    public static ProcessAvailability notFound(String processIdentifier) {
        return new ProcessAvailability(processIdentifier, Status.NOT_FOUND);
    }

    /**
     * Checks if the process is in the "UP" state.
     *
     * @return true if the process status is "UP"; false otherwise
     */
    public boolean isUp() {
        return status == Status.UP;
    }

    /**
     * Checks if the current status is "DOWN".
     *
     * @return true if the status is "DOWN", false otherwise
     */
    public boolean isDown() {
        return status == Status.DOWN;
    }

    /**
     * Checks if the status of the process is "not found."
     *
     * @return true if the status is "NOT_FOUND," false otherwise
     */
    public boolean isNotFound() {
        return status == Status.NOT_FOUND;
    }

    public enum Status {
        UP, DOWN, NOT_FOUND;

        public static Status from(Boolean isUp) {
            if (isUp == null) {
                return NOT_FOUND;
            }
            return isUp ? UP : DOWN;
        }
    }
}
