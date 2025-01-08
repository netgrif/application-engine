package com.netgrif.application.engine.workflow.domain.dataset.logic.action;

import com.netgrif.application.engine.workflow.domain.dataset.Field;

public enum SetDataType {
    VALUE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return change != null && change.getValue() != null;
        }
    },
    DEFAULT_VALUE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    INIT {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    VALIDATION {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    TITLE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    DESCRIPTION {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    PLACEHOLDER {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    BEHAVIOR {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    IMMEDIATE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    COMPONENT {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    ALLOWED_NET {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    CHOICE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    OPTION {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    },
    ROLE {
        @Override
        public boolean isTriggered(Field<?> change) {
            return false;
        }
    };

    public abstract boolean isTriggered(Field<?> change);
}
