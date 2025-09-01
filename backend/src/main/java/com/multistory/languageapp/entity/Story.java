package com.multistory.languageapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stories", indexes = {
    @Index(name = "idx_story_language", columnList = "language"),
    @Index(name = "idx_story_difficulty", columnList = "difficulty"),
    @Index(name = "idx_story_active", columnList = "is_active"),
    @Index(name = "idx_story_created", columnList = "created_at"),
    @Index(name = "idx_story_lang_diff", columnList = "language, difficulty")
})
@EntityListeners(AuditingEntityListener.class)
public class Story {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Content is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @NotBlank(message = "Language is required")
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    @Column(nullable = false)
    private String language;
    
    @NotNull(message = "Difficulty level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;
    
    @Size(max = 500, message = "Audio URL must not exceed 500 characters")
    private String audioUrl;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes
    
    @Column(name = "vocabulary_count")
    private Integer vocabularyCount;
    
    @ElementCollection
    @CollectionTable(name = "story_tags", joinColumns = @JoinColumn(name = "story_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chapter> chapters = new ArrayList<>();
    
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProgress> userProgresses = new ArrayList<>();
    
    // Constructors
    public Story() {}
    
    public Story(String title, String content, String language, DifficultyLevel difficulty) {
        this.title = title;
        this.content = content;
        this.language = language;
        this.difficulty = difficulty;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public DifficultyLevel getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public Integer getVocabularyCount() {
        return vocabularyCount;
    }
    
    public void setVocabularyCount(Integer vocabularyCount) {
        this.vocabularyCount = vocabularyCount;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public List<Chapter> getChapters() {
        return chapters;
    }
    
    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
    
    public List<UserProgress> getUserProgresses() {
        return userProgresses;
    }
    
    public void setUserProgresses(List<UserProgress> userProgresses) {
        this.userProgresses = userProgresses;
    }
    
    // Helper methods
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setStory(this);
    }
    
    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
        chapter.setStory(null);
    }
}
