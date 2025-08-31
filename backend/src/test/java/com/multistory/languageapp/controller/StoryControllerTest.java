package com.multistory.languageapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multistory.languageapp.dto.StoryDTO;
import com.multistory.languageapp.dto.StoryCreateRequest;
import com.multistory.languageapp.service.StoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoryController.class)
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryService storyService;

    @Autowired
    private ObjectMapper objectMapper;

    private StoryDTO sampleStoryDTO;
    private StoryCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        sampleStoryDTO = new StoryDTO();
        sampleStoryDTO.setId(1L);
        sampleStoryDTO.setTitle("Café Love Story");
        sampleStoryDTO.setContent("Emma walked into the small café...");
        sampleStoryDTO.setLanguage("en");
        sampleStoryDTO.setDifficulty("INTERMEDIATE");
        sampleStoryDTO.setAudioUrl("https://example.com/audio.mp3");
        sampleStoryDTO.setEstimatedDuration(25);
        sampleStoryDTO.setVocabularyCount(45);
        sampleStoryDTO.setTags(Arrays.asList("romance", "daily_conversation"));
        sampleStoryDTO.setCreatedAt(LocalDateTime.now());

        sampleCreateRequest = new StoryCreateRequest();
        sampleCreateRequest.setTitle("New Story");
        sampleCreateRequest.setContent("Once upon a time...");
        sampleCreateRequest.setLanguage("en");
        sampleCreateRequest.setDifficulty("BEGINNER");
        sampleCreateRequest.setTags(Arrays.asList("adventure", "fantasy"));
    }

    @Test
    void getAllStories_ShouldReturnPageOfStories() throws Exception {
        // Given
        List<StoryDTO> stories = Arrays.asList(sampleStoryDTO);
        Page<StoryDTO> page = new PageImpl<>(stories);
        when(storyService.getStoriesWithFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/stories")
                .param("language", "en")
                .param("difficulty", "INTERMEDIATE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Café Love Story"))
                .andExpect(jsonPath("$.content[0].language").value("en"));
    }

    @Test
    void getStoryById_ShouldReturnStory_WhenStoryExists() throws Exception {
        // Given
        when(storyService.getStoryById(1L)).thenReturn(sampleStoryDTO);

        // When & Then
        mockMvc.perform(get("/api/stories/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Café Love Story"))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.difficulty").value("INTERMEDIATE"));
    }

    @Test
    void createStory_ShouldReturnCreatedStory_WhenValidRequest() throws Exception {
        // Given
        when(storyService.createStory(any(StoryCreateRequest.class))).thenReturn(sampleStoryDTO);

        // When & Then
        mockMvc.perform(post("/api/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Café Love Story"));
    }

    @Test
    void createStory_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        StoryCreateRequest invalidRequest = new StoryCreateRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStory_ShouldReturnUpdatedStory_WhenValidRequest() throws Exception {
        // Given
        when(storyService.updateStory(eq(1L), any())).thenReturn(sampleStoryDTO);

        // When & Then
        mockMvc.perform(put("/api/stories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteStory_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/stories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getStoriesByLanguage_ShouldReturnStoriesForLanguage() throws Exception {
        // Given
        List<StoryDTO> stories = Arrays.asList(sampleStoryDTO);
        when(storyService.getStoriesByLanguage("en")).thenReturn(stories);

        // When & Then
        mockMvc.perform(get("/api/stories/language/en")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].language").value("en"));
    }

    @Test
    void getPopularStories_ShouldReturnPopularStories() throws Exception {
        // Given
        List<StoryDTO> popularStories = Arrays.asList(sampleStoryDTO);
        when(storyService.getPopularStories(10)).thenReturn(popularStories);

        // When & Then
        mockMvc.perform(get("/api/stories/popular")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void searchStories_ShouldReturnMatchingStories() throws Exception {
        // Given
        List<StoryDTO> searchResults = Arrays.asList(sampleStoryDTO);
        when(storyService.searchStories("café")).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/stories/search")
                .param("keyword", "café")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Café Love Story"));
    }

    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/stories/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Story service is running"));
    }
}
