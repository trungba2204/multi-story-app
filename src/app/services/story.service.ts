import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { Story, StoryFilter, UserProgress, Quiz, QuizResult } from '../models/story.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StoryService {
  private readonly apiUrl = `${environment.apiUrl}/api`;
  private currentStorySubject = new BehaviorSubject<Story | null>(null);
  public currentStory$ = this.currentStorySubject.asObservable();
  
  // Cache for stories and individual story
  private storiesCache = new Map<string, { data: Story[], timestamp: number }>();
  private storyCache = new Map<number, { data: Story, timestamp: number }>();
  private readonly CACHE_DURATION = 2 * 60 * 1000; // 2 minutes for better performance

  constructor(private http: HttpClient) {}

  // Story CRUD Operations
  getStories(filter?: StoryFilter): Observable<Story[]> {
    // Create cache key from filter
    const cacheKey = this.createCacheKey(filter);
    
    // Check cache first
    const cached = this.storiesCache.get(cacheKey);
    if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
      console.log('Returning cached stories for key:', cacheKey);
      return of(cached.data);
    }

    let params = new HttpParams();
    
    if (filter) {
      if (filter.language) params = params.set('language', filter.language);
      if (filter.difficulty) params = params.set('difficulty', filter.difficulty);
      if (filter.keyword) params = params.set('keyword', filter.keyword);
      if (filter.page) params = params.set('page', filter.page.toString());
      if (filter.size) params = params.set('size', filter.size.toString());
    }

    console.log('Fetching fresh stories for key:', cacheKey);
    return this.http.get<any>(`${this.apiUrl}/stories`, { params })
      .pipe(
        map(response => response.content || response),
        tap(stories => {
          // Cache the results
          this.storiesCache.set(cacheKey, {
            data: stories,
            timestamp: Date.now()
          });
        }),
        catchError(this.handleError)
      );
  }

  getStoryById(id: number): Observable<Story> {
    // Check cache first
    const cached = this.storyCache.get(id);
    if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
      console.log('Returning cached story for ID:', id);
      return of(cached.data);
    }

    console.log('Fetching fresh story for ID:', id);
    return this.http.get<Story>(`${this.apiUrl}/stories/${id}`)
      .pipe(
        tap(story => {
          // Cache the story
          this.storyCache.set(id, {
            data: story,
            timestamp: Date.now()
          });
        }),
        catchError(this.handleError)
      );
  }

  getStoriesByLanguage(language: string): Observable<Story[]> {
    return this.http.get<Story[]>(`${this.apiUrl}/stories/language/${language}`)
      .pipe(catchError(this.handleError));
  }

  getPopularStories(limit: number = 10): Observable<Story[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Story[]>(`${this.apiUrl}/stories/popular`, { params })
      .pipe(catchError(this.handleError));
  }

  searchStories(keyword: string): Observable<Story[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<Story[]>(`${this.apiUrl}/stories/search`, { params })
      .pipe(catchError(this.handleError));
  }

  // Current Story Management
  setCurrentStory(story: Story): void {
    this.currentStorySubject.next(story);
  }

  getCurrentStory(): Story | null {
    return this.currentStorySubject.value;
  }

  clearCurrentStory(): void {
    this.currentStorySubject.next(null);
  }

  // User Progress Operations
  getUserProgress(userId: number): Observable<UserProgress[]> {
    return this.http.get<UserProgress[]>(`${this.apiUrl}/progress/user/${userId}`)
      .pipe(catchError(this.handleError));
  }

  getUserStoryProgress(userId: number, storyId: number): Observable<UserProgress> {
    return this.http.get<UserProgress>(`${this.apiUrl}/progress/user/${userId}/story/${storyId}`)
      .pipe(catchError(this.handleError));
  }

  updateProgress(userId: number, storyId: number, progressData: Partial<UserProgress>): Observable<UserProgress> {
    return this.http.post<UserProgress>(`${this.apiUrl}/progress/user/${userId}/story/${storyId}`, progressData)
      .pipe(catchError(this.handleError));
  }

  startStory(userId: number, storyId: number): Observable<UserProgress> {
    return this.http.post<UserProgress>(`${this.apiUrl}/progress/user/${userId}/story/${storyId}/start`, {})
      .pipe(catchError(this.handleError));
  }

  completeChapter(userId: number, storyId: number, chapterNumber: number, progressData?: any): Observable<UserProgress> {
    return this.http.post<UserProgress>(
      `${this.apiUrl}/progress/user/${userId}/story/${storyId}/chapter/${chapterNumber}/complete`, 
      progressData || {}
    ).pipe(catchError(this.handleError));
  }

  // Quiz Operations
  getQuizForStory(storyId: number, chapterId?: number): Observable<any[]> {
    const url = chapterId 
      ? `${this.apiUrl}/quiz/story/${storyId}/chapter/${chapterId}`
      : `${this.apiUrl}/quiz/story/${storyId}`;
    
    return this.http.get<any[]>(url)
      .pipe(catchError(this.handleError));
  }

  submitQuizAnswers(userId: number, quizId: number, answers: any[]): Observable<QuizResult> {
    return this.http.post<QuizResult>(`${this.apiUrl}/quiz/${quizId}/submit`, {
      userId,
      answers
    }).pipe(catchError(this.handleError));
  }

  // Vocabulary Operations
  addVocabularyWord(userId: number, storyId: number, word: string): Observable<UserProgress> {
    return this.http.post<UserProgress>(`${this.apiUrl}/progress/user/${userId}/story/${storyId}/vocabulary`, word)
      .pipe(catchError(this.handleError));
  }

  getUserVocabulary(userId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/progress/user/${userId}/vocabulary`)
      .pipe(catchError(this.handleError));
  }

  // Pronunciation Operations
  updatePronunciationScore(userId: number, storyId: number, score: number): Observable<UserProgress> {
    return this.http.post<UserProgress>(`${this.apiUrl}/progress/user/${userId}/story/${storyId}/pronunciation`, score)
      .pipe(catchError(this.handleError));
  }

  // Utility Methods
  private handleError(error: any): Observable<never> {
    console.error('StoryService Error:', error);
    throw error;
  }

  // Helper methods for UI
  getDifficultyColor(difficulty: string): string {
    switch (difficulty.toLowerCase()) {
      case 'beginner': return '#4CAF50';
      case 'elementary': return '#8BC34A';
      case 'intermediate': return '#FF9800';
      case 'advanced': return '#F44336';
      case 'proficient': return '#9C27B0';
      default: return '#757575';
    }
  }

  getDifficultyLabel(difficulty: string): string {
    return difficulty.charAt(0).toUpperCase() + difficulty.slice(1).toLowerCase();
  }

  formatDuration(minutes: number): string {
    if (minutes < 60) {
      return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${hours}h ${remainingMinutes}m`;
  }

  getProgressPercentage(progress: UserProgress): number {
    return progress?.completionPercentage || 0;
  }

  isStoryCompleted(progress: UserProgress): boolean {
    return progress?.isCompleted || false;
  }

  getNextChapter(progress: UserProgress, story: Story): number {
    if (!progress || !story.chapters) return 1;
    
    if (progress.isCompleted) {
      return story.chapters.length;
    }
    
    return Math.min(progress.currentChapter + 1, story.chapters.length);
  }

  // Cache helper methods
  private createCacheKey(filter?: StoryFilter): string {
    if (!filter) return 'default';
    
    const keyParts = [
      filter.language || 'all',
      filter.difficulty || 'all',
      filter.keyword || '',
      filter.page?.toString() || '0',
      filter.size?.toString() || '20'
    ];
    
    return keyParts.join('|');
  }

  clearCache(): void {
    console.log('Clearing all caches');
    this.storiesCache.clear();
    this.storyCache.clear();
  }

  refreshStories(filter?: StoryFilter): Observable<Story[]> {
    // Clear specific cache entry
    const cacheKey = this.createCacheKey(filter);
    console.log('Refreshing stories - clearing cache for key:', cacheKey);
    this.storiesCache.delete(cacheKey);
    console.log('Cache cleared, fetching fresh data...');
    return this.getStories(filter);
  }
}
