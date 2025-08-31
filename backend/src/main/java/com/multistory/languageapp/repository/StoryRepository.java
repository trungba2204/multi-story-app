package com.multistory.languageapp.repository;

import com.multistory.languageapp.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    // Find active stories
    List<Story> findByIsActiveTrue();
    
    // Find stories by language
    List<Story> findByLanguageAndIsActiveTrue(String language);
    
    // Find stories by difficulty level
    @Query("SELECT s FROM Story s WHERE s.difficulty = :difficulty AND s.isActive = true")
    List<Story> findByDifficultyAndIsActiveTrue(@Param("difficulty") String difficulty);
    
    // Find stories by language and difficulty
    @Query("SELECT s FROM Story s WHERE s.language = :language AND s.difficulty = :difficulty AND s.isActive = true")
    List<Story> findByLanguageAndDifficultyAndIsActiveTrue(
        @Param("language") String language, 
        @Param("difficulty") String difficulty
    );
    
    // Find stories by tags
    @Query("SELECT DISTINCT s FROM Story s JOIN s.tags t WHERE t IN :tags AND s.isActive = true")
    List<Story> findByTagsInAndIsActiveTrue(@Param("tags") List<String> tags);
    
    // Search stories by title or content
    @Query("SELECT s FROM Story s WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND s.isActive = true")
    List<Story> searchByTitleOrContent(@Param("keyword") String keyword);
    
    // Paginated search with filters
    @Query("SELECT s FROM Story s WHERE " +
           "(:language IS NULL OR s.language = :language) AND " +
           "(:difficulty IS NULL OR s.difficulty = :difficulty) AND " +
           "(:keyword IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "s.isActive = true")
    Page<Story> findStoriesWithFilters(
        @Param("language") String language,
        @Param("difficulty") String difficulty,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    // Find story with chapters
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.chapters c WHERE s.id = :id AND s.isActive = true ORDER BY c.chapterNumber")
    Optional<Story> findByIdWithChapters(@Param("id") Long id);
    
    // Get stories ordered by creation date
    List<Story> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Count stories by language
    long countByLanguageAndIsActiveTrue(String language);
    
    // Find popular stories (you might implement this based on user progress completion rates)
    @Query("SELECT s FROM Story s WHERE s.isActive = true ORDER BY " +
           "(SELECT COUNT(up) FROM UserProgress up WHERE up.story = s AND up.isCompleted = true) DESC")
    List<Story> findPopularStories(Pageable pageable);
}
