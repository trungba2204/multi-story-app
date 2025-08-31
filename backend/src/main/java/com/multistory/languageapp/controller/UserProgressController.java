package com.multistory.languageapp.controller;

import com.multistory.languageapp.dto.UserProgressDTO;
import com.multistory.languageapp.dto.UserProgressUpdateRequest;
import com.multistory.languageapp.dto.UserStatisticsDTO;
import com.multistory.languageapp.service.UserProgressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class UserProgressController {
    
    private final UserProgressService userProgressService;
    
    @Autowired
    public UserProgressController(UserProgressService userProgressService) {
        this.userProgressService = userProgressService;
    }
    
    /**
     * Get all progress for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserProgressDTO>> getUserProgress(@PathVariable Long userId) {
        List<UserProgressDTO> progress = userProgressService.getUserProgress(userId);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Get progress for specific user and story
     */
    @GetMapping("/user/{userId}/story/{storyId}")
    public ResponseEntity<UserProgressDTO> getUserStoryProgress(
            @PathVariable Long userId,
            @PathVariable Long storyId) {
        
        UserProgressDTO progress = userProgressService.getUserStoryProgress(userId, storyId);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Create or update user progress
     */
    @PostMapping("/user/{userId}/story/{storyId}")
    public ResponseEntity<UserProgressDTO> updateProgress(
            @PathVariable Long userId,
            @PathVariable Long storyId,
            @Valid @RequestBody UserProgressUpdateRequest request) {
        
        UserProgressDTO progress = userProgressService.updateProgress(userId, storyId, request);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Get user's completed stories
     */
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<UserProgressDTO>> getCompletedStories(@PathVariable Long userId) {
        List<UserProgressDTO> completedStories = userProgressService.getCompletedStories(userId);
        return ResponseEntity.ok(completedStories);
    }
    
    /**
     * Get user's in-progress stories
     */
    @GetMapping("/user/{userId}/in-progress")
    public ResponseEntity<List<UserProgressDTO>> getInProgressStories(@PathVariable Long userId) {
        List<UserProgressDTO> inProgressStories = userProgressService.getInProgressStories(userId);
        return ResponseEntity.ok(inProgressStories);
    }
    
    /**
     * Get user's learning statistics
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@PathVariable Long userId) {
        UserStatisticsDTO statistics = userProgressService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get user's vocabulary learned
     */
    @GetMapping("/user/{userId}/vocabulary")
    public ResponseEntity<List<String>> getUserVocabulary(@PathVariable Long userId) {
        List<String> vocabulary = userProgressService.getUserVocabulary(userId);
        return ResponseEntity.ok(vocabulary);
    }
    
    /**
     * Add vocabulary word to user's learned list
     */
    @PostMapping("/user/{userId}/story/{storyId}/vocabulary")
    public ResponseEntity<UserProgressDTO> addVocabularyWord(
            @PathVariable Long userId,
            @PathVariable Long storyId,
            @RequestBody String word) {
        
        UserProgressDTO progress = userProgressService.addVocabularyWord(userId, storyId, word);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Update pronunciation score
     */
    @PostMapping("/user/{userId}/story/{storyId}/pronunciation")
    public ResponseEntity<UserProgressDTO> updatePronunciationScore(
            @PathVariable Long userId,
            @PathVariable Long storyId,
            @RequestBody Double score) {
        
        UserProgressDTO progress = userProgressService.updatePronunciationScore(userId, storyId, score);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Get user's current active story
     */
    @GetMapping("/user/{userId}/current")
    public ResponseEntity<UserProgressDTO> getCurrentActiveStory(@PathVariable Long userId) {
        UserProgressDTO currentStory = userProgressService.getCurrentActiveStory(userId);
        return ResponseEntity.ok(currentStory);
    }
    
    /**
     * Start a new story for user
     */
    @PostMapping("/user/{userId}/story/{storyId}/start")
    public ResponseEntity<UserProgressDTO> startStory(
            @PathVariable Long userId,
            @PathVariable Long storyId) {
        
        UserProgressDTO progress = userProgressService.startStory(userId, storyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }
    
    /**
     * Complete a chapter
     */
    @PostMapping("/user/{userId}/story/{storyId}/chapter/{chapterNumber}/complete")
    public ResponseEntity<UserProgressDTO> completeChapter(
            @PathVariable Long userId,
            @PathVariable Long storyId,
            @PathVariable Integer chapterNumber,
            @RequestBody(required = false) UserProgressUpdateRequest request) {
        
        UserProgressDTO progress = userProgressService.completeChapter(userId, storyId, chapterNumber, request);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Get leaderboard data
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<?>> getLeaderboard() {
        List<?> leaderboard = userProgressService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * Reset user progress for a story
     */
    @DeleteMapping("/user/{userId}/story/{storyId}")
    public ResponseEntity<Void> resetStoryProgress(
            @PathVariable Long userId,
            @PathVariable Long storyId) {
        
        userProgressService.resetStoryProgress(userId, storyId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete all user progress (GDPR compliance)
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserProgress(@PathVariable Long userId) {
        userProgressService.deleteUserProgress(userId);
        return ResponseEntity.noContent().build();
    }
}
