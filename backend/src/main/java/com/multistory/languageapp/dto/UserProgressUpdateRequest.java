package com.multistory.languageapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public class UserProgressUpdateRequest {
    
    @Min(value = 0, message = "Completion percentage must be at least 0")
    @Max(value = 100, message = "Completion percentage must not exceed 100")
    private Integer completionPercentage;
    
    private Integer currentChapter;
    private Integer timeSpent;
    
    @Min(value = 0, message = "Quiz score must be at least 0")
    @Max(value = 100, message = "Quiz score must not exceed 100")
    private Integer quizScore;
    
    private List<String> vocabularyLearned;
    private Double pronunciationScore;

    // Constructors
    public UserProgressUpdateRequest() {}

    // Getters and Setters
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

    public Double getPronunciationScore() { return pronunciationScore; }
    public void setPronunciationScore(Double pronunciationScore) { this.pronunciationScore = pronunciationScore; }
}
