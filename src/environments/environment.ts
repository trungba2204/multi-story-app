export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'ws://localhost:8080',
  features: {
    offlineMode: true,
    voiceRecognition: true,
    realTimeSync: true,
    analytics: false
  },
  audio: {
    maxCacheSize: 100, // MB
    preloadNextChapter: true,
    defaultPlaybackRate: 1.0
  },
  quiz: {
    timeLimit: 300, // seconds
    maxAttempts: 3,
    passingScore: 70
  }
};
