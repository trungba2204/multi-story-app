package com.multistory.languageapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "chapters")
public class Chapter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Chapter title is required")
    @Size(max = 255, message = "Chapter title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;
    
    @NotNull(message = "Chapter number is required")
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    @NotBlank(message = "Chapter content is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Size(max = 500, message = "Audio URL must not exceed 500 characters")
    @Column(name = "audio_url")
    private String audioUrl;
    
    @Column(name = "duration") // in seconds
    private Integer duration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
    
    @Column(name = "is_unlocked")
    private Boolean isUnlocked = false;
    
    // Constructors
    public Chapter() {}
    
    public Chapter(String title, Integer chapterNumber, String content, Story story) {
        this.title = title;
        this.chapterNumber = chapterNumber;
        this.content = content;
        this.story = story;
        this.isUnlocked = chapterNumber == 1; // First chapter is always unlocked
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
    
    public Integer getChapterNumber() {
        return chapterNumber;
    }
    
    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Story getStory() {
        return story;
    }
    
    public void setStory(Story story) {
        this.story = story;
    }
    
    public Boolean getIsUnlocked() {
        return isUnlocked;
    }
    
    public void setIsUnlocked(Boolean isUnlocked) {
        this.isUnlocked = isUnlocked;
    }
}
