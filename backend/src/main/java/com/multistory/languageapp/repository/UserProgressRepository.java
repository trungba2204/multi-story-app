package com.multistory.languageapp.repository;

import com.multistory.languageapp.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    // Find user progress by user ID
    List<UserProgress> findByUserId(Long userId);
    
    // Find user progress for specific story
    Optional<UserProgress> findByUserIdAndStoryId(Long userId, Long storyId);
    
    // Find completed stories for user
    List<UserProgress> findByUserIdAndIsCompletedTrue(Long userId);
    
    // Find in-progress stories for user
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.status = 'IN_PROGRESS'")
    List<UserProgress> findInProgressByUserId(@Param("userId") Long userId);
    
    // Get user's learning statistics
    @Query("SELECT " +
           "COUNT(up) as totalStories, " +
           "COUNT(CASE WHEN up.isCompleted = true THEN 1 END) as completedStories, " +
           "AVG(up.completionPercentage) as avgCompletion, " +
           "SUM(up.timeSpent) as totalTimeSpent, " +
           "AVG(up.quizScore) as avgQuizScore " +
           "FROM UserProgress up WHERE up.userId = :userId")
    Object[] getUserStatistics(@Param("userId") Long userId);
    
    // Find recent progress updates
    List<UserProgress> findByUserIdAndLastAccessedAfterOrderByLastAccessedDesc(
        Long userId, LocalDateTime since
    );
    
    // Get user's vocabulary learning progress
    @Query("SELECT DISTINCT vl FROM UserProgress up JOIN up.vocabularyLearned vl WHERE up.userId = :userId")
    List<String> findVocabularyLearnedByUserId(@Param("userId") Long userId);
    
    // Count total vocabulary words learned by user
    @Query("SELECT COUNT(DISTINCT vl) FROM UserProgress up JOIN up.vocabularyLearned vl WHERE up.userId = :userId")
    Long countVocabularyLearnedByUserId(@Param("userId") Long userId);
    
    // Find users who completed a specific story
    List<UserProgress> findByStoryIdAndIsCompletedTrue(Long storyId);
    
    // Get average completion rate for a story
    @Query("SELECT AVG(up.completionPercentage) FROM UserProgress up WHERE up.story.id = :storyId")
    Double getAverageCompletionRateForStory(@Param("storyId") Long storyId);
    
    // Find users with low engagement (not accessed recently)
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.lastAccessed < :threshold AND up.isCompleted = false")
    List<UserProgress> findStaleProgress(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold);
    
    // Get user's current active story (most recently accessed incomplete story)
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.isCompleted = false " +
           "ORDER BY up.lastAccessed DESC")
    List<UserProgress> findCurrentActiveStory(@Param("userId") Long userId);
    
    // Delete user progress (for GDPR compliance)
    void deleteByUserId(Long userId);
    
    // Get leaderboard data (top performers by completion rate)
    @Query("SELECT up.userId, AVG(up.completionPercentage) as avgCompletion, COUNT(up) as totalStories " +
           "FROM UserProgress up GROUP BY up.userId ORDER BY avgCompletion DESC")
    List<Object[]> getLeaderboardData();
}
