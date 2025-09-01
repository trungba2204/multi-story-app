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
  styleUrls: ['./story-list.component.scss'],
})
export class StoryListComponent implements OnInit, OnDestroy {
  stories: Story[] = [];
  filteredStories: Story[] = [];
  loading = false;
  error: string | null = null;

  // Filter properties
  filter: StoryFilter = {
    page: 0,
    size: 20,
  };

  searchKeyword = '';
  selectedLanguage = '';
  selectedDifficulty: DifficultyLevel | '' = '';
  selectedTags: string[] = [];

  // Available options
  languages = ['en', 'ja', 'ko', 'es', 'fr', 'de'];
  difficulties = Object.values(DifficultyLevel);
  availableTags = [
    'romance',
    'mystery',
    'adventure',
    'daily_life',
    'business',
    'travel',
  ];

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  constructor(public storyService: StoryService, private router: Router) {
    // Setup search debouncing - reduced time for faster response
    this.searchSubject
      .pipe(
        debounceTime(150), // Reduced from 300ms to 150ms
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((keyword) => {
        this.filter.keyword = keyword;
        this.loadStories();
      });
  }

  ngOnInit(): void {
    console.log('StoryListComponent ngOnInit - checking cache first');

    // Force loading state immediately
    this.loading = true;

    // Load stories immediately - service will check cache first
    this.loadStories();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadStories(): void {
    // Only show loading if we don't have stories yet OR if loading is already true (from refresh)
    if (this.stories.length === 0 || this.loading) {
      this.loading = true;
    }
    this.error = null;
    this.loading = true;

    console.log('LoadStories called - current loading state:', this.loading);
    this.storyService.getStories(this.filter).subscribe({
      next: (stories) => {
        console.log(
          'LoadStories success - received:',
          stories.length,
          'stories'
        );
        this.stories = stories;
        this.filteredStories = stories;
        this.loading = false;
        console.log('Stories loaded and loading set to false');
      },
      error: (error) => {
        this.error = 'Failed to load stories. Please try again.';
        this.loading = false;
        console.error('Error loading stories:', error);
      },
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
    this.filter.tags =
      this.selectedTags.length > 0 ? this.selectedTags : undefined;
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
    console.log('Navigating to story:', story.id, story.title);

    // Preload story data before navigation for faster loading
    this.storyService.getStoryById(story.id).subscribe({
      next: (storyData) => {
        console.log('Story preloaded:', storyData.title);
      },
      error: (err) => {
        console.log('Preload failed, will load normally:', err);
      },
    });

    // Set current story and navigate
    this.storyService.setCurrentStory(story);

    this.router
      .navigate(['/story', story.id])
      .then((success) => {
        if (success) {
          console.log('Navigation successful to story:', story.id);
        } else {
          console.error('Navigation failed for story:', story.id);
        }
      })
      .catch((error) => {
        console.error('Navigation error:', error);
      });
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
        },
      });
    }
  }

  retry(): void {
    this.error = null;
    this.loadStories();
  }

  refreshStories(): void {
    console.log('=== REFRESH BUTTON CLICKED ===');

    // Immediately show loading state
    this.loading = true;
    this.error = null;

    console.log('Loading state set to true, clearing cache...');

    // Force clear cache
    this.storyService.clearCache();

    // Get fresh stories
    this.storyService.getStories(this.filter).subscribe({
      next: (stories) => {
        console.log('=== REFRESH SUCCESS ===');
        console.log('Fresh stories received:', stories.length);

        // Update stories and stop loading
        this.stories = stories;
        this.filteredStories = stories;
        this.loading = false;

        console.log('Refresh completed successfully');
      },
      error: (error) => {
        console.error('=== REFRESH ERROR ===', error);
        this.error = 'Failed to refresh stories. Please try again.';
        this.loading = false;
      },
    });
  }
}
