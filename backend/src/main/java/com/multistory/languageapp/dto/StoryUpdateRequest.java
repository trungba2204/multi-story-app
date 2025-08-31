package com.multistory.languageapp.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public class StoryUpdateRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    private String content;
    
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;
    
    private String difficulty;
    
    @Size(max = 500, message = "Audio URL must not exceed 500 characters")
    private String audioUrl;
    
    private Integer estimatedDuration;
    private Integer vocabularyCount;
    private List<String> tags;
    private Boolean isActive;

    // Constructors
    public StoryUpdateRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Integer getVocabularyCount() { return vocabularyCount; }
    public void setVocabularyCount(Integer vocabularyCount) { this.vocabularyCount = vocabularyCount; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
