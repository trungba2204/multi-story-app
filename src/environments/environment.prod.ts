export const environment = {
  production: true,
  apiUrl: 'https://api.multistoryapp.com',
  wsUrl: 'wss://api.multistoryapp.com',
  features: {
    offlineMode: true,
    voiceRecognition: true,
    realTimeSync: true,
    analytics: true
  },
  audio: {
    maxCacheSize: 200, // MB
    preloadNextChapter: true,
    defaultPlaybackRate: 1.0
  },
  quiz: {
    timeLimit: 300, // seconds
    maxAttempts: 3,
    passingScore: 70
  }
};
