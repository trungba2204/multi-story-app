import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Story } from '../../models/story.model';
import { StoryService } from '../../services/story.service';

interface SimpleQuiz {
  id: number;
  storyId: number;
  questions: any[];
}

@Component({
  selector: 'app-quiz',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <!-- Super Simple Quiz -->
    <div class="simple-quiz">
      
      <!-- Loading -->
      <div *ngIf="loading" class="center">
        <h2>⏳ Loading quiz...</h2>
      </div>

      <!-- Error -->
      <div *ngIf="error" class="center">
        <h2>❌ {{ error }}</h2>
        <button (click)="retry()">Try Again</button>
      </div>

      <!-- Quiz Content -->
      <div *ngIf="quiz && !loading && !error && currentQuestion" class="quiz-content">
        
        <!-- Header -->
        <div class="quiz-header">
          <h1>📝 Quiz: {{ story?.title }}</h1>
          <p>Question {{ currentQuestionIndex + 1 }} of {{ quiz.questions.length }}</p>
          <div class="progress-bar">
            <div class="progress-fill" [style.width.%]="getProgressPercentage()"></div>
          </div>
        </div>

        <!-- Current Question -->
        <div class="question-section">
          <h3>{{ currentQuestion.question }}</h3>
          
          <!-- Multiple Choice -->
          <div *ngIf="currentQuestion.type === 'multiple_choice'" class="options">
            <div *ngFor="let option of currentQuestion.options; let i = index" class="option">
              <input 
                type="radio" 
                [id]="'option-' + i" 
                [value]="i" 
                [(ngModel)]="currentAnswer"
                name="answer">
              <label [for]="'option-' + i">{{ getOptionLetter(i) }}. {{ option }}</label>
            </div>
          </div>
          
          <!-- Fill in Blank -->
          <div *ngIf="currentQuestion.type === 'fill_blank'" class="fill-blank">
            <input 
              type="text" 
              [(ngModel)]="currentAnswer" 
              placeholder="Type your answer..."
              class="text-input">
          </div>
          
          <!-- True/False -->
          <div *ngIf="currentQuestion.type === 'true_false'" class="true-false">
            <div class="option">
              <input 
                type="radio" 
                id="true-option" 
                [value]="true" 
                [(ngModel)]="currentAnswer" 
                name="answer">
              <label for="true-option">✅ True</label>
            </div>
            <div class="option">
              <input 
                type="radio" 
                id="false-option" 
                [value]="false" 
                [(ngModel)]="currentAnswer" 
                name="answer">
              <label for="false-option">❌ False</label>
            </div>
          </div>
        </div>

        <!-- Navigation -->
        <div class="quiz-navigation">
          <button 
            *ngIf="currentQuestionIndex > 0" 
            (click)="previousQuestion()" 
            class="btn-secondary">
            ⬅️ Previous
          </button>
          
          <button 
            *ngIf="currentQuestionIndex < quiz.questions.length - 1" 
            (click)="nextQuestion()" 
            class="btn-primary"
            [disabled]="!currentAnswer">
            Next ➡️
          </button>
          
          <button 
            *ngIf="currentQuestionIndex === quiz.questions.length - 1" 
            (click)="submitQuiz()" 
            class="btn-success"
            [disabled]="!currentAnswer">
            Submit Quiz 🚀
          </button>
        </div>
      </div>

      <!-- Results -->
      <div *ngIf="result && !loading" class="quiz-results">
        <h2>🎉 Quiz Complete!</h2>
        <div class="score-display">
          <div class="score-circle" [style.color]="getScoreColor()">
            {{ result.percentage.toFixed(0) }}%
          </div>
          <p><strong>Score:</strong> {{ result.score }} / {{ result.maxScore }}</p>
          <p class="message">{{ result.message }}</p>
        </div>
        
        <div class="result-actions">
          <a routerLink="/stories" class="btn-primary">📚 More Stories</a>
          <button (click)="retakeQuiz()" class="btn-secondary">🔄 Retake Quiz</button>
        </div>
      </div>
      
    </div>
  `,
  styles: [`
    .simple-quiz {
      max-width: 800px;
      margin: 20px auto;
      padding: 20px;
      font-family: system-ui, sans-serif;
    }
    
    .center {
      text-align: center;
      padding: 40px;
    }
    
    .quiz-header {
      text-align: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #eee;
    }
    
    .quiz-header h1 {
      margin: 0 0 10px 0;
      color: #333;
    }
    
    .progress-bar {
      width: 100%;
      height: 8px;
      background: #f0f0f0;
      border-radius: 4px;
      margin-top: 15px;
    }
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #4CAF50, #45a049);
      border-radius: 4px;
      transition: width 0.3s ease;
    }
    
    .question-section {
      background: white;
      padding: 30px;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      margin-bottom: 30px;
    }
    
    .question-section h3 {
      margin: 0 0 20px 0;
      color: #333;
      font-size: 1.3em;
    }
    
    .options .option {
      margin: 15px 0;
      display: flex;
      align-items: center;
    }
    
    .options input[type="radio"] {
      margin-right: 10px;
      transform: scale(1.2);
    }
    
    .options label {
      font-size: 1.1em;
      cursor: pointer;
      flex: 1;
      padding: 10px;
      border-radius: 5px;
      transition: background 0.2s;
    }
    
    .options label:hover {
      background: #f5f5f5;
    }
    
    .text-input {
      width: 100%;
      padding: 15px;
      font-size: 1.1em;
      border: 2px solid #ddd;
      border-radius: 8px;
      margin-top: 10px;
    }
    
    .text-input:focus {
      outline: none;
      border-color: #4CAF50;
    }
    
    .true-false .option {
      margin: 20px 0;
      display: flex;
      align-items: center;
    }
    
    .quiz-navigation {
      display: flex;
      gap: 15px;
      justify-content: center;
      flex-wrap: wrap;
    }
    
    .btn-primary, .btn-secondary, .btn-success {
      padding: 12px 24px;
      border: none;
      border-radius: 6px;
      font-size: 1em;
      cursor: pointer;
      text-decoration: none;
      display: inline-block;
      transition: all 0.2s;
    }
    
    .btn-primary {
      background: #007bff;
      color: white;
    }
    
    .btn-primary:hover {
      background: #0056b3;
    }
    
    .btn-secondary {
      background: #6c757d;
      color: white;
    }
    
    .btn-secondary:hover {
      background: #545b62;
    }
    
    .btn-success {
      background: #28a745;
      color: white;
    }
    
    .btn-success:hover {
      background: #1e7e34;
    }
    
    .btn-primary:disabled, .btn-success:disabled {
      background: #ccc;
      cursor: not-allowed;
    }
    
    .quiz-results {
      text-align: center;
      background: white;
      padding: 40px;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .score-circle {
      font-size: 3em;
      font-weight: bold;
      margin: 20px 0;
    }
    
    .message {
      font-size: 1.2em;
      margin: 20px 0;
    }
    
    .result-actions {
      display: flex;
      gap: 15px;
      justify-content: center;
      margin-top: 30px;
    }
    
    button {
      padding: 10px 20px;
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    
    button:hover {
      background: #c82333;
    }
  `]
})
export class QuizComponent implements OnInit {
  story: Story | null = null;
  quiz: SimpleQuiz | null = null;
  loading = false;
  error: string | null = null;
  
  currentQuestionIndex = 0;
  currentQuestion: any = null;
  currentAnswer: any = null;
  answers: any[] = [];
  result: any = null;

  private route = inject(ActivatedRoute);
  private storyService = inject(StoryService);

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const storyId = +params['id'];
      if (storyId) {
        this.loadQuiz(storyId);
      }
    });
  }

  loadQuiz(storyId: number): void {
    this.loading = true;
    this.error = null;

    // Load story first  
    this.storyService.getStoryById(storyId).subscribe({
      next: (story) => this.story = story,
      error: (err) => console.error('Story load error:', err)
    });
    
    // Load quiz (API returns array directly)
    this.storyService.getQuizForStory(storyId).subscribe({
      next: (quizQuestions: any[]) => {
        if (quizQuestions && quizQuestions.length > 0) {
          this.quiz = { 
            id: storyId, 
            storyId: storyId, 
            questions: quizQuestions 
          };
          this.currentQuestion = this.quiz.questions[0];
          this.currentQuestionIndex = 0;
        } else {
          this.error = 'No quiz available for this story';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load quiz';
        this.loading = false;
        console.error('Quiz load error:', err);
      }
    });
  }

  nextQuestion(): void {
    this.saveCurrentAnswer();
    if (this.currentQuestionIndex < this.quiz!.questions.length - 1) {
      this.currentQuestionIndex++;
      this.currentQuestion = this.quiz!.questions[this.currentQuestionIndex];
      this.currentAnswer = null;
    }
  }

  previousQuestion(): void {
    this.saveCurrentAnswer();
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.currentQuestion = this.quiz!.questions[this.currentQuestionIndex];
      // Load existing answer if available
      const existingAnswer = this.answers[this.currentQuestionIndex];
      this.currentAnswer = existingAnswer || null;
    }
  }

  saveCurrentAnswer(): void {
    if (this.currentAnswer !== null) {
      this.answers[this.currentQuestionIndex] = this.currentAnswer;
    }
  }

  submitQuiz(): void {
    this.saveCurrentAnswer();
    this.loading = true;

    const answersForSubmit = this.quiz!.questions.map((question, index) => ({
      questionId: question.id,
      userAnswer: this.answers[index] || null,
      isCorrect: this.checkAnswer(question, this.answers[index]),
      timeSpent: 5 // Mock time
    }));

    this.storyService.submitQuizAnswers(1, this.quiz!.id, answersForSubmit).subscribe({
      next: (result) => {
        this.result = result;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to submit quiz';
        this.loading = false;
        console.error('Submit error:', err);
      }
    });
  }

  checkAnswer(question: any, userAnswer: any): boolean {
    if (userAnswer === null || userAnswer === undefined) return false;
    
    switch (question.type) {
      case 'multiple_choice':
        return +userAnswer === question.correctAnswer;
      case 'true_false':
        return !!userAnswer === question.correctAnswer;
      case 'fill_blank':
        return String(userAnswer).toLowerCase().trim() === String(question.correctAnswer).toLowerCase().trim();
      default:
        return false;
    }
  }

  retakeQuiz(): void {
    this.quiz = null;
    this.result = null;
    this.currentQuestionIndex = 0;
    this.currentAnswer = null;
    this.answers = [];
    this.error = null;
    
    if (this.story) {
      this.loadQuiz(this.story.id);
    }
  }

  retry(): void {
    if (this.story) {
      this.loadQuiz(this.story.id);
    }
  }

  getProgressPercentage(): number {
    if (!this.quiz) return 0;
    return Math.round(((this.currentQuestionIndex + 1) / this.quiz.questions.length) * 100);
  }

  getOptionLetter(index: number): string {
    return String.fromCharCode(65 + index);
  }

  getScoreColor(): string {
    if (!this.result) return '#333';
    const percentage = this.result.percentage;
    if (percentage >= 80) return '#4CAF50'; // Green
    if (percentage >= 60) return '#FF9800'; // Orange
    return '#F44336'; // Red
  }
}