package com.multistory.languageapp.service;

import com.multistory.languageapp.dto.UserProgressDTO;
import com.multistory.languageapp.dto.UserProgressUpdateRequest;
import com.multistory.languageapp.dto.UserStatisticsDTO;
import com.multistory.languageapp.entity.UserProgress;
import com.multistory.languageapp.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProgressService {
    
    @Autowired
    private UserProgressRepository userProgressRepository;
    
    public List<UserProgressDTO> getUserProgress(Long userId) {
        List<UserProgress> progressList = userProgressRepository.findByUserId(userId);
        return progressList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public UserProgressDTO getUserStoryProgress(Long userId, Long storyId) {
        UserProgress progress = userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .orElseThrow(() -> new RuntimeException("Progress not found for user: " + userId + " and story: " + storyId));
        return convertToDTO(progress);
    }
    
    public UserProgressDTO updateProgress(Long userId, Long storyId, UserProgressUpdateRequest request) {
        UserProgress progress = userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .orElse(new UserProgress(userId, null)); // Story would need to be fetched
        
        updateProgressFromRequest(progress, request);
        progress.setLastAccessed(LocalDateTime.now());
        
        UserProgress savedProgress = userProgressRepository.save(progress);
        return convertToDTO(savedProgress);
    }
    
    public List<UserProgressDTO> getCompletedStories(Long userId) {
        List<UserProgress> completedStories = userProgressRepository.findByUserIdAndIsCompletedTrue(userId);
        return completedStories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<UserProgressDTO> getInProgressStories(Long userId) {
        List<UserProgress> inProgressStories = userProgressRepository.findInProgressByUserId(userId);
        return inProgressStories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public UserStatisticsDTO getUserStatistics(Long userId) {
        Object[] stats = userProgressRepository.getUserStatistics(userId);
        UserStatisticsDTO statisticsDTO = new UserStatisticsDTO();
        // Map the statistics array to DTO properties
        // This is a simplified implementation
        return statisticsDTO;
    }
    
    public List<String> getUserVocabulary(Long userId) {
        return userProgressRepository.findVocabularyLearnedByUserId(userId);
    }
    
    public UserProgressDTO addVocabularyWord(Long userId, Long storyId, String word) {
        UserProgress progress = userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        progress.addVocabularyWord(word);
        UserProgress savedProgress = userProgressRepository.save(progress);
        return convertToDTO(savedProgress);
    }
    
    public UserProgressDTO updatePronunciationScore(Long userId, Long storyId, Double score) {
        UserProgress progress = userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        progress.updatePronunciationScore(score);
        UserProgress savedProgress = userProgressRepository.save(progress);
        return convertToDTO(savedProgress);
    }
    
    public UserProgressDTO getCurrentActiveStory(Long userId) {
        List<UserProgress> activeStories = userProgressRepository.findCurrentActiveStory(userId);
        if (activeStories.isEmpty()) {
            throw new RuntimeException("No active story found for user: " + userId);
        }
        return convertToDTO(activeStories.get(0));
    }
    
    public UserProgressDTO startStory(Long userId, Long storyId) {
        UserProgress progress = new UserProgress(userId, null); // Story would need to be fetched
        progress.setLastAccessed(LocalDateTime.now());
        
        UserProgress savedProgress = userProgressRepository.save(progress);
        return convertToDTO(savedProgress);
    }
    
    public UserProgressDTO completeChapter(Long userId, Long storyId, Integer chapterNumber, UserProgressUpdateRequest request) {
        UserProgress progress = userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        progress.setCurrentChapter(chapterNumber);
        if (request != null) {
            updateProgressFromRequest(progress, request);
        }
        
        UserProgress savedProgress = userProgressRepository.save(progress);
        return convertToDTO(savedProgress);
    }
    
    public List<?> getLeaderboard() {
        return userProgressRepository.getLeaderboardData();
    }
    
    public void resetStoryProgress(Long userId, Long storyId) {
        userProgressRepository.findByUserIdAndStoryId(userId, storyId)
            .ifPresent(userProgressRepository::delete);
    }
    
    public void deleteUserProgress(Long userId) {
        userProgressRepository.deleteByUserId(userId);
    }
    
    private UserProgressDTO convertToDTO(UserProgress progress) {
        UserProgressDTO dto = new UserProgressDTO();
        dto.setId(progress.getId());
        dto.setUserId(progress.getUserId());
        dto.setStoryId(progress.getStory() != null ? progress.getStory().getId() : null);
        dto.setCompletionPercentage(progress.getCompletionPercentage());
        dto.setCurrentChapter(progress.getCurrentChapter());
        dto.setTimeSpent(progress.getTimeSpent());
        dto.setQuizScore(progress.getQuizScore());
        dto.setVocabularyLearned(progress.getVocabularyLearned());
        dto.setPronunciationAttempts(progress.getPronunciationAttempts());
        dto.setAvgPronunciationScore(progress.getAvgPronunciationScore());
        dto.setLastAccessed(progress.getLastAccessed());
        dto.setIsCompleted(progress.getIsCompleted());
        dto.setStatus(progress.getStatus().name());
        dto.setCreatedAt(progress.getCreatedAt());
        dto.setUpdatedAt(progress.getUpdatedAt());
        return dto;
    }
    
    private void updateProgressFromRequest(UserProgress progress, UserProgressUpdateRequest request) {
        if (request.getCompletionPercentage() != null) {
            progress.setCompletionPercentage(request.getCompletionPercentage());
        }
        if (request.getCurrentChapter() != null) {
            progress.setCurrentChapter(request.getCurrentChapter());
        }
        if (request.getTimeSpent() != null) {
            progress.setTimeSpent(progress.getTimeSpent() + request.getTimeSpent());
        }
        if (request.getQuizScore() != null) {
            progress.setQuizScore(request.getQuizScore());
        }
        if (request.getVocabularyLearned() != null) {
            request.getVocabularyLearned().forEach(progress::addVocabularyWord);
        }
        if (request.getPronunciationScore() != null) {
            progress.updatePronunciationScore(request.getPronunciationScore());
        }
    }
}
