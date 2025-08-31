import { Routes } from '@angular/router';
import { StoryListComponent } from './components/story-list/story-list.component';
import { StoryReaderComponent } from './components/story-reader/story-reader.component';
import { QuizComponent } from './components/quiz/quiz.component';

export const routes: Routes = [
  { path: '', redirectTo: '/stories', pathMatch: 'full' },
  { path: 'stories', component: StoryListComponent },
  { path: 'story/:id', component: StoryReaderComponent },
  { path: 'story/:id/read', component: StoryReaderComponent },
  { path: 'story/:id/quiz', component: QuizComponent },
  { path: '**', redirectTo: '/stories' }
];
