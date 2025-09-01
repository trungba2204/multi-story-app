import { Routes } from '@angular/router';
import { StoryListComponent } from './components/story-list/story-list.component';
import { StoryReaderComponent } from './components/story-reader/story-reader.component';
import { QuizComponent } from './components/quiz/quiz.component';

export const routes: Routes = [
  { path: '', redirectTo: '/stories', pathMatch: 'full' },
  { path: 'stories', component: StoryListComponent },
  { 
    path: 'story/:id', 
    component: StoryReaderComponent,
    runGuardsAndResolvers: 'paramsOrQueryParamsChange'
  },
  { 
    path: 'story/:id/read', 
    component: StoryReaderComponent,
    runGuardsAndResolvers: 'paramsOrQueryParamsChange'
  },
  { 
    path: 'story/:id/quiz', 
    component: QuizComponent,
    runGuardsAndResolvers: 'paramsOrQueryParamsChange'
  },
  { path: '**', redirectTo: '/stories' }
];
