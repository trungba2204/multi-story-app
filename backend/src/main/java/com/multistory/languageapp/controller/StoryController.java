package com.multistory.languageapp.controller;

import com.multistory.languageapp.dto.StoryDTO;
import com.multistory.languageapp.dto.StoryCreateRequest;
import com.multistory.languageapp.dto.StoryUpdateRequest;
import com.multistory.languageapp.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class StoryController {
    
    private final StoryService storyService;
    
    @Autowired
    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }
    
    /**
     * Get all stories with optional filtering and pagination
     */
    @GetMapping
    public ResponseEntity<Page<StoryDTO>> getAllStories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        
        Page<StoryDTO> stories = storyService.getStoriesWithFilters(language, difficulty, keyword, pageable);
        return ResponseEntity.ok(stories);
    }
    
    /**
     * Get story by ID with chapters
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoryDTO> getStoryById(@PathVariable Long id) {
        StoryDTO story = storyService.getStoryById(id);
        return ResponseEntity.ok(story);
    }
    
    /**
     * Create new story
     */
    @PostMapping
    public ResponseEntity<StoryDTO> createStory(@Valid @RequestBody StoryCreateRequest request) {
        StoryDTO createdStory = storyService.createStory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStory);
    }
    
    /**
     * Update existing story
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoryDTO> updateStory(
            @PathVariable Long id,
            @Valid @RequestBody StoryUpdateRequest request) {
        
        StoryDTO updatedStory = storyService.updateStory(id, request);
        return ResponseEntity.ok(updatedStory);
    }
    
    /**
     * Delete story (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get stories by language
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<List<StoryDTO>> getStoriesByLanguage(@PathVariable String language) {
        List<StoryDTO> stories = storyService.getStoriesByLanguage(language);
        return ResponseEntity.ok(stories);
    }
    
    /**
     * Get popular stories
     */
    @GetMapping("/popular")
    public ResponseEntity<List<StoryDTO>> getPopularStories(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<StoryDTO> popularStories = storyService.getPopularStories(limit);
        return ResponseEntity.ok(popularStories);
    }
    
    /**
     * Search stories by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<StoryDTO>> searchStories(@RequestParam String keyword) {
        List<StoryDTO> stories = storyService.searchStories(keyword);
        return ResponseEntity.ok(stories);
    }
    
    /**
     * Get stories by tags
     */
    @GetMapping("/tags")
    public ResponseEntity<List<StoryDTO>> getStoriesByTags(@RequestParam List<String> tags) {
        List<StoryDTO> stories = storyService.getStoriesByTags(tags);
        return ResponseEntity.ok(stories);
    }
    
    /**
     * Get story statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStoryStatistics(@PathVariable Long id) {
        // Return completion rates, user engagement, etc.
        var stats = storyService.getStoryStatistics(id);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Story service is running");
    }
}
