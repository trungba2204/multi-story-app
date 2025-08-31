# Multi-Story Language App - System Architecture

## Backend Microservices

### 1. User Service (Node.js + Express)
```javascript
// Responsibilities:
- User authentication & authorization
- Profile management
- Subscription management
- Social features (friends, leaderboard)

// Tech Stack:
- Runtime: Node.js 18+
- Framework: Express.js/Fastify
- Authentication: JWT + Refresh Token
- Password: bcrypt
- Validation: Joi/Yup
```

### 2. Content Service (Python + FastAPI)
```python
# Responsibilities:
- Story management (CRUD)
- Grammar rules & vocabulary
- Content versioning
- Multilingual support

# Tech Stack:
- Runtime: Python 3.11+
- Framework: FastAPI
- ORM: SQLAlchemy
- Serialization: Pydantic
- Background tasks: Celery + Redis
```

### 3. Progress Service (Node.js + Express)
```javascript
// Responsibilities:
- Learning progress tracking
- Analytics & reporting
- Achievement system
- Study streak calculation

// Features:
- Real-time progress updates
- Data aggregation for insights
- Performance metrics
```

### 4. AI Service (Python + FastAPI)
```python
# Responsibilities:
- Story recommendations
- Interactive dialogue generation
- Difficulty adaptation
- Learning path optimization

# AI Components:
- Recommendation Engine: Collaborative Filtering
- NLP: OpenAI GPT-4 API
- Vector Search: Pinecone/Weaviate
- ML Models: TensorFlow/PyTorch
```

### 5. Voice Service (Go)
```go
// Responsibilities:
- Speech-to-text processing
- Pronunciation scoring
- Audio file management
- Voice synthesis

// Tech Stack:
- Language: Go 1.21+
- Framework: Gin/Fiber
- Speech APIs: Google Cloud Speech
- Audio Processing: FFmpeg
```

## Database Design

### PostgreSQL (Primary Database)
```sql
-- Core Tables
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    subscription_tier VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE stories (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    difficulty_level INTEGER,
    content JSONB,
    audio_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_progress (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    story_id UUID REFERENCES stories(id),
    completion_percentage INTEGER,
    last_accessed TIMESTAMP,
    vocabulary_learned JSONB
);
```

### Redis (Caching & Sessions)
```redis
# Session management
SET session:user_id:token "{user_data}" EX 3600

# Caching frequently accessed content
SET story:trending "story_ids_array" EX 1800

# Rate limiting
INCR rate_limit:user_id:endpoint EX 60
```

### MongoDB (Content & Logs)
```javascript
// Stories with rich content
{
  "_id": ObjectId("..."),
  "title": "The Mystery Café",
  "language": "en",
  "chapters": [
    {
      "id": 1,
      "text": "...",
      "audio_segments": [...],
      "interactive_points": [...]
    }
  ],
  "metadata": {
    "difficulty": "intermediate",
    "topics": ["daily_conversation", "food"],
    "estimated_duration": 15
  }
}

// User interaction logs
{
  "user_id": "...",
  "action": "pronunciation_attempt",
  "story_id": "...",
  "timestamp": ISODate("..."),
  "score": 85,
  "audio_url": "..."
}
```

## API Design Examples

### RESTful APIs
```javascript
// Content Service APIs
GET /api/v1/stories?language=en&difficulty=intermediate
POST /api/v1/stories
PUT /api/v1/stories/:id
DELETE /api/v1/stories/:id

// Progress Service APIs  
GET /api/v1/users/:id/progress
POST /api/v1/progress/update
GET /api/v1/analytics/dashboard

// Voice Service APIs
POST /api/v1/voice/recognize
POST /api/v1/voice/synthesize
GET /api/v1/voice/pronunciation-score
```

### WebSocket APIs
```javascript
// Real-time interactive dialogue
ws://voice-service/dialogue/:story_id
// Events: user_speech, ai_response, pronunciation_feedback

// Live progress updates
ws://progress-service/live-updates/:user_id
// Events: progress_update, achievement_unlocked, streak_update
```

## Security Implementation

### 1. Authentication & Authorization
```javascript
// JWT Implementation
const generateTokens = (user) => {
  const accessToken = jwt.sign(
    { userId: user.id, role: user.role },
    process.env.JWT_SECRET,
    { expiresIn: '15m' }
  );
  
  const refreshToken = jwt.sign(
    { userId: user.id },
    process.env.REFRESH_SECRET,
    { expiresIn: '7d' }
  );
  
  return { accessToken, refreshToken };
};

// Role-based access control
const authorize = (roles) => (req, res, next) => {
  if (!roles.includes(req.user.role)) {
    return res.status(403).json({ error: 'Insufficient permissions' });
  }
  next();
};
```

### 2. Data Protection
```python
# Input validation & sanitization
from pydantic import BaseModel, validator
import bleach

class StoryCreate(BaseModel):
    title: str
    content: str
    
    @validator('title', 'content')
    def sanitize_text(cls, v):
        return bleach.clean(v, tags=[], strip=True)

# SQL Injection prevention
query = """
    SELECT * FROM stories 
    WHERE language = %s AND difficulty = %s
"""
cursor.execute(query, (language, difficulty))
```

### 3. API Security
```javascript
// Rate limiting
const rateLimit = require('express-rate-limit');

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP'
});

// CORS configuration
const corsOptions = {
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
  optionsSuccessStatus: 200
};
```

### 4. Infrastructure Security
```yaml
# Docker security
FROM node:18-alpine
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001
USER nextjs

# Environment variables
DATABASE_URL=postgresql://encrypted_connection
API_KEYS=encrypted_in_vault
SECRETS_MANAGER=aws_secrets_manager
```

## Deployment & DevOps

### Container Orchestration
```yaml
# docker-compose.yml
version: '3.8'
services:
  user-service:
    build: ./services/user-service
    environment:
      - NODE_ENV=production
      - DATABASE_URL=${USER_DB_URL}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  content-service:
    build: ./services/content-service
    environment:
      - PYTHON_ENV=production
      - DATABASE_URL=${CONTENT_DB_URL}
```

### Monitoring & Logging
```javascript
// Application monitoring
const prometheus = require('prom-client');

const httpRequestsTotal = new prometheus.Counter({
  name: 'http_requests_total',
  help: 'Total number of HTTP requests',
  labelNames: ['method', 'route', 'status']
});

// Structured logging
const winston = require('winston');

const logger = winston.createLogger({
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'app.log' }),
    new winston.transports.Console()
  ]
});
```

## Performance Optimization

### Caching Strategy
```javascript
// Multi-level caching
// 1. CDN caching for static assets
// 2. Redis for database query results
// 3. Application-level memory cache

const NodeCache = require('node-cache');
const cache = new NodeCache({ stdTTL: 600 }); // 10 minutes

const getCachedStories = async (language, difficulty) => {
  const key = `stories:${language}:${difficulty}`;
  
  let stories = cache.get(key);
  if (!stories) {
    stories = await database.getStories(language, difficulty);
    cache.set(key, stories);
  }
  
  return stories;
};
```

### Database Optimization
```sql
-- Indexing strategy
CREATE INDEX idx_stories_language_difficulty 
ON stories(language, difficulty_level);

CREATE INDEX idx_user_progress_user_story 
ON user_progress(user_id, story_id);

-- Partitioning for large tables
CREATE TABLE user_logs_2024 PARTITION OF user_logs
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

## Scalability Considerations

1. **Horizontal Scaling**: Microservices có thể scale độc lập
2. **Database Sharding**: Chia user data theo region
3. **CDN**: Global distribution cho audio/video files
4. **Auto-scaling**: Kubernetes HPA dựa trên CPU/Memory
5. **Load Balancing**: Round-robin và health checks

## Cost Optimization

```javascript
// Resource usage monitoring
const costOptimization = {
  audioCompression: 'opus 64kbps', // Giảm 70% size vs WAV
  videoCaching: '30 days TTL',     // Giảm bandwidth cost
  dbConnections: 'connection pooling', // Tối ưu database cost
  computeInstances: 'spot instances' // Giảm 50-70% cost
};
```

Kiến trúc này đảm bảo:
- **Scalability**: Có thể mở rộng từng service độc lập
- **Reliability**: Fault tolerance và disaster recovery
- **Security**: Multi-layer security approach
- **Performance**: Optimized cho mobile và web
- **Cost-effective**: Resource optimization và monitoring
