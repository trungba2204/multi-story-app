export interface Story {
  id: number;
  title: string;
  content: string;
  language: string;
  difficulty: DifficultyLevel;
  audioUrl?: string;
  estimatedDuration?: number;
  vocabularyCount?: number;
  tags: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt?: Date;
  chapters?: Chapter[];
}

export interface Chapter {
  id: number;
  title: string;
  chapterNumber: number;
  content: string;
  audioUrl?: string;
  duration?: number;
  isUnlocked: boolean;
}

export interface UserProgress {
  id: number;
  userId: number;
  storyId: number;
  completionPercentage: number;
  currentChapter: number;
  timeSpent: number;
  quizScore?: number;
  vocabularyLearned: string[];
  pronunciationAttempts: number;
  avgPronunciationScore?: number;
  lastAccessed: Date;
  isCompleted: boolean;
  status: ProgressStatus;
}

export interface Quiz {
  id: number;
  storyId: number;
  chapterId?: number;
  questions: QuizQuestion[];
}

export interface QuizQuestion {
  id: number;
  type: QuestionType;
  question: string;
  options?: string[];
  correctAnswer: string | number;
  explanation?: string;
  points: number;
}

export interface QuizResult {
  totalScore: number;
  totalQuestions: number;
  correctAnswers: number;
  answers: QuizAnswer[];
  feedback: string[];
  recommendations: string[];
}

export interface QuizAnswer {
  questionId: number;
  userAnswer: string | number;
  isCorrect: boolean;
  timeSpent: number;
}

export interface VocabularyWord {
  word: string;
  definition: string;
  pronunciation: string;
  audioUrl?: string;
  example: string;
  translation?: string;
  partOfSpeech: string;
}

export enum DifficultyLevel {
  BEGINNER = 'BEGINNER',
  ELEMENTARY = 'ELEMENTARY',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  PROFICIENT = 'PROFICIENT'
}

export enum ProgressStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  PAUSED = 'PAUSED'
}

export enum QuestionType {
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  FILL_IN_BLANK = 'FILL_IN_BLANK',
  TRUE_FALSE = 'TRUE_FALSE',
  LISTENING_COMPREHENSION = 'LISTENING_COMPREHENSION',
  PRONUNCIATION = 'PRONUNCIATION'
}

export interface StoryFilter {
  language?: string;
  difficulty?: DifficultyLevel;
  tags?: string[];
  keyword?: string;
  page?: number;
  size?: number;
}

export interface AudioPlayerState {
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  playbackRate: number;
  isLoading: boolean;
}

export interface PronunciationFeedback {
  overallScore: number;
  accuracy: number;
  fluency: number;
  completeness: number;
  prosody: number;
  wordLevelFeedback: WordFeedback[];
  suggestions: string[];
}

export interface WordFeedback {
  word: string;
  score: number;
  issues: string[];
  suggestion: string;
}
