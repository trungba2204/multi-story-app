import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { StoryService } from '../../services/story.service';
import { Story } from '../../models/story.model';

@Component({
  selector: 'app-story-reader',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="simple-reader">
      
      <!-- Loading -->
      <div *ngIf="loading" class="center">
        <h2>⏳ Loading story...</h2>
      </div>

      <!-- Error -->
      <div *ngIf="error" class="center">
        <h2>❌ {{ error }}</h2>
        <button (click)="retry()">Try Again</button>
      </div>

      <!-- Story Content -->
      <div *ngIf="story && !loading && !error" class="story-content">
        
        <!-- Header -->
        <div class="story-header">
          <h1>📖 {{ story.title }}</h1>
          <div class="story-meta">
            <span class="language">{{ story.language }}</span>
            <span class="difficulty">{{ story.difficulty }}</span>
            <span class="duration">{{ story.estimatedDuration }}s</span>
            <span class="words">{{ story.vocabularyCount }} words</span>
          </div>
        </div>

        <!-- Story Text -->
        <div class="story-text">
          <p>{{ story.content }}</p>
        </div>

        <!-- Reading Controls -->
        <div class="reading-controls">
          <div class="font-controls">
            <button (click)="decreaseFontSize()" class="font-btn">A-</button>
            <span>Font Size</span>
            <button (click)="increaseFontSize()" class="font-btn">A+</button>
          </div>
        </div>

        <!-- Actions -->
        <div class="story-actions">
          <a routerLink="/stories" class="btn btn-secondary">🔙 Back to Stories</a>
          <a [routerLink]="['/story', story.id, 'quiz']" class="btn btn-primary">📝 Take Quiz</a>
        </div>
      </div>
      
    </div>
  `,
  styles: [`
    .simple-reader {
      max-width: 800px;
      margin: 20px auto;
      padding: 20px;
      font-family: system-ui, sans-serif;
      line-height: 1.6;
    }
    
    .center {
      text-align: center;
      padding: 40px;
    }
    
    .center h2 {
      margin: 0 0 20px 0;
      color: #333;
    }
    
    .story-content {
      background: white;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      overflow: hidden;
    }
    
    .story-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 30px;
      text-align: center;
    }
    
    .story-header h1 {
      margin: 0 0 15px 0;
      font-size: 2.2em;
      font-weight: 300;
    }
    
    .story-meta {
      display: flex;
      justify-content: center;
      gap: 20px;
      flex-wrap: wrap;
      font-size: 0.9em;
      opacity: 0.9;
    }
    
    .story-meta span {
      background: rgba(255,255,255,0.2);
      padding: 5px 12px;
      border-radius: 20px;
      backdrop-filter: blur(10px);
    }
    
    .story-text {
      padding: 40px;
      font-size: var(--font-size, 18px);
      color: #333;
      text-align: justify;
    }
    
    .story-text p {
      margin: 0;
      line-height: 1.8;
    }
    
    .reading-controls {
      padding: 20px 40px;
      border-top: 1px solid #eee;
      background: #f9f9f9;
    }
    
    .font-controls {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 15px;
    }
    
    .font-btn {
      background: #007bff;
      color: white;
      border: none;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      font-size: 18px;
      cursor: pointer;
      transition: all 0.2s;
    }
    
    .font-btn:hover {
      background: #0056b3;
      transform: scale(1.1);
    }
    
    .story-actions {
      padding: 30px;
      display: flex;
      gap: 15px;
      justify-content: center;
      flex-wrap: wrap;
      background: #f8f9fa;
    }
    
    .btn {
      padding: 12px 24px;
      border-radius: 6px;
      text-decoration: none;
      font-weight: 500;
      transition: all 0.2s;
      border: none;
      cursor: pointer;
      font-size: 1em;
    }
    
    .btn-primary {
      background: #28a745;
      color: white;
    }
    
    .btn-primary:hover {
      background: #218838;
      transform: translateY(-2px);
    }
    
    .btn-secondary {
      background: #6c757d;
      color: white;
    }
    
    .btn-secondary:hover {
      background: #545b62;
      transform: translateY(-2px);
    }
    
    button {
      padding: 10px 20px;
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1em;
    }
    
    button:hover {
      background: #c82333;
    }
    
    @media (max-width: 768px) {
      .simple-reader {
        margin: 10px;
        padding: 10px;
      }
      
      .story-header h1 {
        font-size: 1.8em;
      }
      
      .story-meta {
        gap: 10px;
      }
      
      .story-text {
        padding: 20px;
        font-size: 16px;
      }
      
      .story-actions {
        flex-direction: column;
        align-items: center;
      }
      
      .btn {
        width: 100%;
        max-width: 200px;
      }
    }
  `]
})
export class StoryReaderComponent implements OnInit {
  story: Story | null = null;
  loading = false;
  error: string | null = null;
  private storyId: number = 0;
  private fontSize = 18;

  private route = inject(ActivatedRoute);
  private storyService = inject(StoryService);

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.storyId = +params['id'];
      if (this.storyId) {
        this.loadStory();
      }
    });
  }

  loadStory(): void {
    this.loading = true;
    this.error = null;

    this.storyService.getStoryById(this.storyId).subscribe({
      next: (story) => {
        this.story = story;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Could not load story. Please try again.';
        this.loading = false;
        console.error('Story load error:', err);
      }
    });
  }

  retry(): void {
    this.loadStory();
  }

  increaseFontSize(): void {
    if (this.fontSize < 24) {
      this.fontSize += 2;
      this.updateFontSize();
    }
  }

  decreaseFontSize(): void {
    if (this.fontSize > 14) {
      this.fontSize -= 2;
      this.updateFontSize();
    }
  }

  private updateFontSize(): void {
    document.documentElement.style.setProperty('--font-size', `${this.fontSize}px`);
  }
}