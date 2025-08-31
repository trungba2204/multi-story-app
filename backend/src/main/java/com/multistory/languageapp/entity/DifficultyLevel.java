package com.multistory.languageapp.entity;

/**
 * Enumeration representing different difficulty levels for language learning stories
 */
public enum DifficultyLevel {
    BEGINNER("Beginner"),
    ELEMENTARY("Elementary"), 
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    PROFICIENT("Proficient");
    
    private final String displayName;
    
    DifficultyLevel(String displayName) {
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
     * Parse string to DifficultyLevel enum
     */
    public static DifficultyLevel fromString(String text) {
        for (DifficultyLevel level : DifficultyLevel.values()) {
            if (level.name().equalsIgnoreCase(text) || 
                level.displayName.equalsIgnoreCase(text)) {
                return level;
            }
        }
        throw new IllegalArgumentException("No difficulty level found for: " + text);
    }
}
