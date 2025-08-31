package com.multistory.languageapp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:4200")
public class QuizController {

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<Map<String, Object>>> getQuizForStory(@PathVariable Long storyId) {
        // Return mock quiz data for now
        List<Map<String, Object>> quiz = new ArrayList<>();
        
        // Multiple choice question
        Map<String, Object> question1 = new HashMap<>();
        question1.put("id", 1);
        question1.put("storyId", storyId);
        question1.put("type", "multiple_choice");
        question1.put("question", "What is the main theme of this story?");
        question1.put("options", Arrays.asList("Adventure", "Romance", "Mystery", "Comedy"));
        question1.put("correctAnswer", 0);
        question1.put("points", 10);
        quiz.add(question1);
        
        // Fill in the blank question
        Map<String, Object> question2 = new HashMap<>();
        question2.put("id", 2);
        question2.put("storyId", storyId);
        question2.put("type", "fill_blank");
        question2.put("question", "The character _____ the challenge successfully.");
        question2.put("correctAnswer", "completed");
        question2.put("points", 15);
        quiz.add(question2);
        
        // True/False question
        Map<String, Object> question3 = new HashMap<>();
        question3.put("id", 3);
        question3.put("storyId", storyId);
        question3.put("type", "true_false");
        question3.put("question", "The story takes place in a modern city.");
        question3.put("correctAnswer", true);
        question3.put("points", 5);
        quiz.add(question3);
        
        return ResponseEntity.ok(quiz);
    }
    
    @PostMapping("/{storyId}/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(
            @PathVariable Long storyId,
            @RequestBody Map<String, Object> requestBody) {
        
        // Extract answers from request body
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) requestBody.get("answers");
        
        // Mock scoring logic
        int totalScore = 0;
        int maxScore = 30; // Sum of all question points
        
        if (answers != null) {
            for (Map<String, Object> answer : answers) {
                // In real implementation, we would validate answers
                // For now, just add points randomly
                totalScore += 10;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("score", Math.min(totalScore, maxScore));
        result.put("maxScore", maxScore);
        result.put("percentage", (double) Math.min(totalScore, maxScore) / maxScore * 100);
        result.put("passed", totalScore >= maxScore * 0.7); // 70% to pass
        result.put("message", totalScore >= maxScore * 0.7 ? "Congratulations! You passed!" : "Keep studying!");
        
        return ResponseEntity.ok(result);
    }
}
