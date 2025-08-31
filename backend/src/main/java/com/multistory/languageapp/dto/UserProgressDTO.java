package com.multistory.languageapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserProgressDTO {
    private Long id;
    private Long userId;
    private Long storyId;
    private Integer completionPercentage;
    private Integer currentChapter;
    private Integer timeSpent;
    private Integer quizScore;
    private List<String> vocabularyLearned;
    private Integer pronunciationAttempts;
    private Double avgPronunciationScore;
    private LocalDateTime lastAccessed;
    private Boolean isCompleted;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserProgressDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getStoryId() { return storyId; }
    public void setStoryId(Long storyId) { this.storyId = storyId; }

    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }

    public Integer getCurrentChapter() { return currentChapter; }
    public void setCurrentChapter(Integer currentChapter) { this.currentChapter = currentChapter; }

    public Integer getTimeSpent() { return timeSpent; }
    public void setTimeSpent(Integer timeSpent) { this.timeSpent = timeSpent; }

    public Integer getQuizScore() { return quizScore; }
    public void setQuizScore(Integer quizScore) { this.quizScore = quizScore; }

    public List<String> getVocabularyLearned() { return vocabularyLearned; }
    public void setVocabularyLearned(List<String> vocabularyLearned) { this.vocabularyLearned = vocabularyLearned; }

    public Integer getPronunciationAttempts() { return pronunciationAttempts; }
    public void setPronunciationAttempts(Integer pronunciationAttempts) { this.pronunciationAttempts = pronunciationAttempts; }

    public Double getAvgPronunciationScore() { return avgPronunciationScore; }
    public void setAvgPronunciationScore(Double avgPronunciationScore) { this.avgPronunciationScore = avgPronunciationScore; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
