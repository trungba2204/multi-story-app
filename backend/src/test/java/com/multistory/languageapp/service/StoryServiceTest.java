package com.multistory.languageapp.service;

import com.multistory.languageapp.dto.StoryCreateRequest;
import com.multistory.languageapp.dto.StoryDTO;
import com.multistory.languageapp.dto.StoryUpdateRequest;
import com.multistory.languageapp.entity.Story;
import com.multistory.languageapp.entity.DifficultyLevel;
import com.multistory.languageapp.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private StoryService storyService;

    private Story sampleStory;
    private StoryCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        sampleStory = new Story();
        sampleStory.setId(1L);
        sampleStory.setTitle("Test Story");
        sampleStory.setContent("This is a test story content...");
        sampleStory.setLanguage("en");
        sampleStory.setDifficulty(DifficultyLevel.INTERMEDIATE);
        sampleStory.setIsActive(true);

        sampleCreateRequest = new StoryCreateRequest();
        sampleCreateRequest.setTitle("New Story");
        sampleCreateRequest.setContent("New story content...");
        sampleCreateRequest.setLanguage("en");
        sampleCreateRequest.setDifficulty("BEGINNER");
        sampleCreateRequest.setTags(Arrays.asList("adventure", "fantasy"));
    }

    @Test
    void getStoryById_ShouldReturnStoryDTO_WhenStoryExists() {
        // Given
        when(storyRepository.findById(1L)).thenReturn(Optional.of(sampleStory));

        // When
        StoryDTO result = storyService.getStoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Story", result.getTitle());
        assertEquals("en", result.getLanguage());
        verify(storyRepository).findById(1L);
    }

    @Test
    void getStoryById_ShouldThrowException_WhenStoryNotFound() {
        // Given
        when(storyRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> storyService.getStoryById(999L));
        verify(storyRepository).findById(999L);
    }

    @Test
    void createStory_ShouldReturnCreatedStoryDTO() {
        // Given
        when(storyRepository.save(any(Story.class))).thenReturn(sampleStory);

        // When
        StoryDTO result = storyService.createStory(sampleCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Story", result.getTitle());
        verify(storyRepository).save(any(Story.class));
    }

    @Test
    void getStoriesByLanguage_ShouldReturnStoriesForSpecificLanguage() {
        // Given
        List<Story> stories = Arrays.asList(sampleStory);
        when(storyRepository.findByLanguageAndIsActiveTrue("en")).thenReturn(stories);

        // When
        List<StoryDTO> result = storyService.getStoriesByLanguage("en");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("en", result.get(0).getLanguage());
        verify(storyRepository).findByLanguageAndIsActiveTrue("en");
    }

    @Test
    void getStoriesWithFilters_ShouldReturnFilteredStories() {
        // Given
        List<Story> stories = Arrays.asList(sampleStory);
        Page<Story> page = new PageImpl<>(stories);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(storyRepository.findStoriesWithFilters(
            eq("en"), eq("INTERMEDIATE"), eq("test"), eq(pageable)))
            .thenReturn(page);

        // When
        Page<StoryDTO> result = storyService.getStoriesWithFilters("en", "INTERMEDIATE", "test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Story", result.getContent().get(0).getTitle());
        verify(storyRepository).findStoriesWithFilters("en", "INTERMEDIATE", "test", pageable);
    }

    @Test
    void searchStories_ShouldReturnMatchingStories() {
        // Given
        List<Story> stories = Arrays.asList(sampleStory);
        when(storyRepository.searchByTitleOrContent("test")).thenReturn(stories);

        // When
        List<StoryDTO> result = storyService.searchStories("test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("test") ||
                  result.get(0).getContent().toLowerCase().contains("test"));
        verify(storyRepository).searchByTitleOrContent("test");
    }

    @Test
    void deleteStory_ShouldMarkStoryAsInactive() {
        // Given
        when(storyRepository.findById(1L)).thenReturn(Optional.of(sampleStory));
        when(storyRepository.save(any(Story.class))).thenReturn(sampleStory);

        // When
        storyService.deleteStory(1L);

        // Then
        verify(storyRepository).findById(1L);
        verify(storyRepository).save(argThat(story -> !story.getIsActive()));
    }

    @Test
    void getPopularStories_ShouldReturnPopularStories() {
        // Given
        List<Story> popularStories = Arrays.asList(sampleStory);
        when(storyRepository.findPopularStories(any(Pageable.class))).thenReturn(popularStories);

        // When
        List<StoryDTO> result = storyService.getPopularStories(5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(storyRepository).findPopularStories(any(Pageable.class));
    }

    @Test
    void updateStory_ShouldUpdateAndReturnStoryDTO() {
        // Given
        when(storyRepository.findById(1L)).thenReturn(Optional.of(sampleStory));
        when(storyRepository.save(any(Story.class))).thenReturn(sampleStory);

        StoryUpdateRequest updateRequest = new StoryUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");

        // When
        StoryDTO result = storyService.updateStory(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(storyRepository).findById(1L);
        verify(storyRepository).save(any(Story.class));
    }

    @Test
    void getStoriesByTags_ShouldReturnStoriesWithMatchingTags() {
        // Given
        List<String> tags = Arrays.asList("romance", "adventure");
        List<Story> stories = Arrays.asList(sampleStory);
        when(storyRepository.findByTagsInAndIsActiveTrue(tags)).thenReturn(stories);

        // When
        List<StoryDTO> result = storyService.getStoriesByTags(tags);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(storyRepository).findByTagsInAndIsActiveTrue(tags);
    }
}
