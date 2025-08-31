package com.multistory.languageapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_progress")
@EntityListeners(AuditingEntityListener.class)
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
    
    @Min(value = 0, message = "Completion percentage must be at least 0")
    @Max(value = 100, message = "Completion percentage must not exceed 100")
    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;
    
    @Column(name = "current_chapter")
    private Integer currentChapter = 1;
    
    @Column(name = "time_spent") // in seconds
    private Integer timeSpent = 0;
    
    @Min(value = 0, message = "Quiz score must be at least 0")
    @Max(value = 100, message = "Quiz score must not exceed 100")
    @Column(name = "quiz_score")
    private Integer quizScore;
    
    @ElementCollection
    @CollectionTable(name = "user_vocabulary_learned", 
                    joinColumns = @JoinColumn(name = "user_progress_id"))
    @Column(name = "vocabulary_word")
    private List<String> vocabularyLearned = new ArrayList<>();
    
    @Column(name = "pronunciation_attempts")
    private Integer pronunciationAttempts = 0;
    
    @Column(name = "avg_pronunciation_score")
    private Double avgPronunciationScore;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserProgress() {}
    
    public UserProgress(Long userId, Story story) {
        this.userId = userId;
        this.story = story;
        this.lastAccessed = LocalDateTime.now();
        this.status = ProgressStatus.IN_PROGRESS;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Story getStory() {
        return story;
    }
    
    public void setStory(Story story) {
        this.story = story;
    }
    
    public Integer getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
        if (completionPercentage >= 100) {
            this.isCompleted = true;
            this.status = ProgressStatus.COMPLETED;
        }
    }
    
    public Integer getCurrentChapter() {
        return currentChapter;
    }
    
    public void setCurrentChapter(Integer currentChapter) {
        this.currentChapter = currentChapter;
    }
    
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public Integer getQuizScore() {
        return quizScore;
    }
    
    public void setQuizScore(Integer quizScore) {
        this.quizScore = quizScore;
    }
    
    public List<String> getVocabularyLearned() {
        return vocabularyLearned;
    }
    
    public void setVocabularyLearned(List<String> vocabularyLearned) {
        this.vocabularyLearned = vocabularyLearned;
    }
    
    public Integer getPronunciationAttempts() {
        return pronunciationAttempts;
    }
    
    public void setPronunciationAttempts(Integer pronunciationAttempts) {
        this.pronunciationAttempts = pronunciationAttempts;
    }
    
    public Double getAvgPronunciationScore() {
        return avgPronunciationScore;
    }
    
    public void setAvgPronunciationScore(Double avgPronunciationScore) {
        this.avgPronunciationScore = avgPronunciationScore;
    }
    
    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }
    
    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public ProgressStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProgressStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public void addVocabularyWord(String word) {
        if (!vocabularyLearned.contains(word)) {
            vocabularyLearned.add(word);
        }
    }
    
    public void updatePronunciationScore(Double newScore) {
        if (avgPronunciationScore == null) {
            avgPronunciationScore = newScore;
        } else {
            // Calculate moving average
            avgPronunciationScore = (avgPronunciationScore * pronunciationAttempts + newScore) 
                                  / (pronunciationAttempts + 1);
        }
        pronunciationAttempts++;
    }
    
    public void updateProgress(Integer newCompletionPercentage, Integer timeSpentInSession) {
        setCompletionPercentage(newCompletionPercentage);
        setTimeSpent(getTimeSpent() + timeSpentInSession);
        setLastAccessed(LocalDateTime.now());
        
        if (status == ProgressStatus.NOT_STARTED) {
            status = ProgressStatus.IN_PROGRESS;
        }
    }
}
