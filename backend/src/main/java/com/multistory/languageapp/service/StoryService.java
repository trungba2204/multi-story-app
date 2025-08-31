package com.multistory.languageapp.service;

import com.multistory.languageapp.dto.StoryDTO;
import com.multistory.languageapp.dto.StoryCreateRequest;
import com.multistory.languageapp.dto.StoryUpdateRequest;
import com.multistory.languageapp.entity.Story;
import com.multistory.languageapp.exception.StoryNotFoundException;
import com.multistory.languageapp.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoryService {
    
    @Autowired
    private StoryRepository storyRepository;
    
    public Page<StoryDTO> getStoriesWithFilters(String language, String difficulty, String keyword, Pageable pageable) {
        Page<Story> stories = storyRepository.findStoriesWithFilters(language, difficulty, keyword, pageable);
        return stories.map(this::convertToDTO);
    }
    
    public StoryDTO getStoryById(Long id) {
        Story story = storyRepository.findById(id)
            .orElseThrow(() -> new StoryNotFoundException("Story not found with id: " + id));
        return convertToDTO(story);
    }
    
    public StoryDTO createStory(StoryCreateRequest request) {
        Story story = convertToEntity(request);
        Story savedStory = storyRepository.save(story);
        return convertToDTO(savedStory);
    }
    
    public StoryDTO updateStory(Long id, StoryUpdateRequest request) {
        Story story = storyRepository.findById(id)
            .orElseThrow(() -> new StoryNotFoundException("Story not found with id: " + id));
        
        updateEntityFromRequest(story, request);
        Story updatedStory = storyRepository.save(story);
        return convertToDTO(updatedStory);
    }
    
    public void deleteStory(Long id) {
        Story story = storyRepository.findById(id)
            .orElseThrow(() -> new StoryNotFoundException("Story not found with id: " + id));
        story.setIsActive(false);
        storyRepository.save(story);
    }
    
    public List<StoryDTO> getStoriesByLanguage(String language) {
        List<Story> stories = storyRepository.findByLanguageAndIsActiveTrue(language);
        return stories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<StoryDTO> getPopularStories(int limit) {
        List<Story> stories = storyRepository.findPopularStories(Pageable.ofSize(limit));
        return stories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<StoryDTO> searchStories(String keyword) {
        List<Story> stories = storyRepository.searchByTitleOrContent(keyword);
        return stories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<StoryDTO> getStoriesByTags(List<String> tags) {
        List<Story> stories = storyRepository.findByTagsInAndIsActiveTrue(tags);
        return stories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public Object getStoryStatistics(Long id) {
        // Placeholder implementation
        return "Statistics for story " + id;
    }
    
    private StoryDTO convertToDTO(Story story) {
        StoryDTO dto = new StoryDTO();
        dto.setId(story.getId());
        dto.setTitle(story.getTitle());
        dto.setContent(story.getContent());
        dto.setLanguage(story.getLanguage());
        dto.setDifficulty(story.getDifficulty().toString());
        dto.setAudioUrl(story.getAudioUrl());
        dto.setEstimatedDuration(story.getEstimatedDuration());
        dto.setVocabularyCount(story.getVocabularyCount());
        dto.setTags(story.getTags());
        dto.setIsActive(story.getIsActive());
        dto.setCreatedAt(story.getCreatedAt());
        dto.setUpdatedAt(story.getUpdatedAt());
        return dto;
    }
    
    private Story convertToEntity(StoryCreateRequest request) {
        Story story = new Story();
        story.setTitle(request.getTitle());
        story.setContent(request.getContent());
        story.setLanguage(request.getLanguage());
        // story.setDifficulty(DifficultyLevel.valueOf(request.getDifficulty()));
        story.setAudioUrl(request.getAudioUrl());
        story.setEstimatedDuration(request.getEstimatedDuration());
        story.setVocabularyCount(request.getVocabularyCount());
        story.setTags(request.getTags());
        return story;
    }
    
    private void updateEntityFromRequest(Story story, StoryUpdateRequest request) {
        if (request.getTitle() != null) story.setTitle(request.getTitle());
        if (request.getContent() != null) story.setContent(request.getContent());
        if (request.getLanguage() != null) story.setLanguage(request.getLanguage());
        if (request.getDifficulty() != null) {
            // story.setDifficulty(DifficultyLevel.valueOf(request.getDifficulty()));
        }
        if (request.getAudioUrl() != null) story.setAudioUrl(request.getAudioUrl());
        if (request.getEstimatedDuration() != null) story.setEstimatedDuration(request.getEstimatedDuration());
        if (request.getVocabularyCount() != null) story.setVocabularyCount(request.getVocabularyCount());
        if (request.getTags() != null) story.setTags(request.getTags());
        if (request.getIsActive() != null) story.setIsActive(request.getIsActive());
    }
}
