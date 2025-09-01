import {
  Component,
  OnInit,
  OnDestroy,
  inject,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { StoryService } from '../../services/story.service';
import { Story } from '../../models/story.model';

@Component({
  selector: 'app-story-reader',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './story-reader.component.html',
  styleUrls: ['./story-reader.component.scss'],
})
export class StoryReaderComponent implements OnInit, OnDestroy {
  story: Story | null = null;
  loading = true; // Always start with loading true
  error: string | null = null;
  storyId: number = 0; // Changed from private to public
  private fontSize = 18;
  private routeSubscription: Subscription | null = null;

  private route = inject(ActivatedRoute);
  private storyService = inject(StoryService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    console.log('StoryReaderComponent ngOnInit - initial loading:', this.loading);
    this.routeSubscription = this.route.params.subscribe((params) => {
      const newStoryId = +params['id'];
      console.log('Route params changed, new story ID:', newStoryId, 'current:', this.storyId);

      if (newStoryId && newStoryId !== this.storyId) {
        console.log('=== LOADING NEW STORY ===');
        console.log('Setting loading = true for story:', newStoryId);

        // Always show loading for new story
        this.loading = true;
        this.error = null;
        this.story = null;
        this.storyId = newStoryId;

        console.log('Loading state after set:', this.loading);
        console.log('Forcing UI update with detectChanges');

        // Force immediate UI update
        this.cdr.detectChanges();

        // Load story immediately
        this.loadStory();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  loadStory(): void {
    console.log('=== LOAD STORY METHOD ===');
    console.log('Loading story with ID:', this.storyId);
    console.log('Current loading state before API call:', this.loading);
    
    // Ensure loading is true
    this.loading = true;
    this.cdr.detectChanges();
    
    this.storyService.getStoryById(this.storyId).subscribe({
      next: (story) => {
        console.log('=== STORY LOADED SUCCESS ===');
        console.log('Story loaded successfully:', story.title);
        this.story = story;
        this.loading = false;
        console.log('Loading set to false, forcing UI update');
        this.cdr.detectChanges(); // Force UI update
      },
      error: (err) => {
        console.error('=== STORY LOAD ERROR ===', err);
        this.error = 'Could not load story. Please try again.';
        this.loading = false;
        console.log('Error occurred, loading set to false');
        this.cdr.detectChanges(); // Force UI update
      },
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
    document.documentElement.style.setProperty(
      '--font-size',
      `${this.fontSize}px`
    );
  }
}
