package com.ihss.scheduler.entity;

/**
 * Types of auditable actions.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    ENABLE,
    DISABLE,
    HOLD,
    RESUME,
    ICE,
    TRIGGER,
    STOP,
    RESTART,
    ADD_DEPENDENCY,
    REMOVE_DEPENDENCY,
    REORDER
}
