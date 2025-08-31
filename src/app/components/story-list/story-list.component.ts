import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Story, StoryFilter, DifficultyLevel } from '../../models/story.model';
import { StoryService } from '../../services/story.service';

@Component({
  selector: 'app-story-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './story-list.component.html',
  styleUrls: ['./story-list.component.scss']
})
export class StoryListComponent implements OnInit, OnDestroy {
  stories: Story[] = [];
  filteredStories: Story[] = [];
  loading = false;
  error: string | null = null;

  // Filter properties
  filter: StoryFilter = {
    page: 0,
    size: 20
  };
  
  searchKeyword = '';
  selectedLanguage = '';
  selectedDifficulty: DifficultyLevel | '' = '';
  selectedTags: string[] = [];
  
  // Available options
  languages = ['en', 'ja', 'ko', 'es', 'fr', 'de'];
  difficulties = Object.values(DifficultyLevel);
  availableTags = ['romance', 'mystery', 'adventure', 'daily_life', 'business', 'travel'];
  
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  constructor(
    public storyService: StoryService,
    private router: Router
  ) {
    // Setup search debouncing
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(keyword => {
      this.filter.keyword = keyword;
      this.loadStories();
    });
  }

  ngOnInit(): void {
    // Load stories immediately without delay
    this.loadStories();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadStories(): void {
    this.loading = true;
    this.error = null;

    this.storyService.getStories(this.filter).subscribe({
      next: (stories) => {
        this.stories = stories;
        this.filteredStories = stories;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load stories. Please try again.';
        this.loading = false;
        console.error('Error loading stories:', error);
      }
    });
  }

  onSearchKeywordChange(keyword: string): void {
    this.searchKeyword = keyword;
    this.searchSubject.next(keyword);
  }

  onLanguageChange(): void {
    this.filter.language = this.selectedLanguage || undefined;
    this.loadStories();
  }

  onDifficultyChange(): void {
    this.filter.difficulty = this.selectedDifficulty || undefined;
    this.loadStories();
  }

  onTagToggle(tag: string): void {
    const index = this.selectedTags.indexOf(tag);
    if (index > -1) {
      this.selectedTags.splice(index, 1);
    } else {
      this.selectedTags.push(tag);
    }
    this.filter.tags = this.selectedTags.length > 0 ? this.selectedTags : undefined;
    this.loadStories();
  }

  isTagSelected(tag: string): boolean {
    return this.selectedTags.includes(tag);
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedLanguage = '';
    this.selectedDifficulty = '';
    this.selectedTags = [];
    this.filter = { page: 0, size: 20 };
    this.loadStories();
  }

  onStoryClick(story: Story): void {
    this.storyService.setCurrentStory(story);
    this.router.navigate(['/story', story.id]);
  }

  onStartStory(story: Story, event: Event): void {
    event.stopPropagation();
    this.storyService.setCurrentStory(story);
    this.router.navigate(['/story', story.id, 'read']);
  }

  getDifficultyClass(difficulty: string): string {
    return `difficulty-${difficulty.toLowerCase()}`;
  }

  getDifficultyColor(difficulty: string): string {
    return this.storyService.getDifficultyColor(difficulty);
  }

  formatDuration(duration: number | undefined): string {
    if (!duration) return 'Unknown';
    return this.storyService.formatDuration(duration);
  }

  getStoryProgress(story: Story): number {
    // This would typically come from user progress data
    // For now, return 0 or implement based on your user state management
    return 0;
  }

  isStoryCompleted(story: Story): boolean {
    // This would typically come from user progress data
    return false;
  }

  isStoryStarted(story: Story): boolean {
    // This would typically come from user progress data
    return false;
  }

  loadMore(): void {
    if (!this.loading) {
      this.filter.page = (this.filter.page || 0) + 1;
      this.loading = true;
      
      this.storyService.getStories(this.filter).subscribe({
        next: (newStories) => {
          this.stories = [...this.stories, ...newStories];
          this.filteredStories = this.stories;
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load more stories.';
          this.loading = false;
          console.error('Error loading more stories:', error);
        }
      });
    }
  }

  retry(): void {
    this.error = null;
    this.loadStories();
  }
}
