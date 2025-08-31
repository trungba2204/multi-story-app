package com.multistory.languageapp.dto;

public class UserStatisticsDTO {
    private Long totalStories;
    private Long completedStories;
    private Double avgCompletion;
    private Long totalTimeSpent;
    private Double avgQuizScore;
    private Long vocabularyWordsLearned;
    private Integer currentStreak;
    private Integer longestStreak;

    // Constructors
    public UserStatisticsDTO() {}

    // Getters and Setters
    public Long getTotalStories() { return totalStories; }
    public void setTotalStories(Long totalStories) { this.totalStories = totalStories; }

    public Long getCompletedStories() { return completedStories; }
    public void setCompletedStories(Long completedStories) { this.completedStories = completedStories; }

    public Double getAvgCompletion() { return avgCompletion; }
    public void setAvgCompletion(Double avgCompletion) { this.avgCompletion = avgCompletion; }

    public Long getTotalTimeSpent() { return totalTimeSpent; }
    public void setTotalTimeSpent(Long totalTimeSpent) { this.totalTimeSpent = totalTimeSpent; }

    public Double getAvgQuizScore() { return avgQuizScore; }
    public void setAvgQuizScore(Double avgQuizScore) { this.avgQuizScore = avgQuizScore; }

    public Long getVocabularyWordsLearned() { return vocabularyWordsLearned; }
    public void setVocabularyWordsLearned(Long vocabularyWordsLearned) { this.vocabularyWordsLearned = vocabularyWordsLearned; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
}
