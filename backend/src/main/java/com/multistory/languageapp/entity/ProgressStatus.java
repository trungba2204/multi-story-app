package com.multistory.languageapp.entity;

/**
 * Enumeration representing the status of user progress through a story
 */
public enum ProgressStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    PAUSED("Paused");
    
    private final String displayName;
    
    ProgressStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Parse string to ProgressStatus enum
     */
    public static ProgressStatus fromString(String text) {
        for (ProgressStatus status : ProgressStatus.values()) {
            if (status.name().equalsIgnoreCase(text) || 
                status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No progress status found for: " + text);
    }
}
