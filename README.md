# Multi-Story Language Learning App

Ứng dụng học ngôn ngữ thông qua câu chuyện tương tác với AI conversation và voice recognition.

## 🏗️ Kiến Trúc Hệ Thống

### Backend - Spring Boot
- **Framework**: Spring Boot 3.2.0 + Java 17
- **Database**: H2 (development), PostgreSQL (production)  
- **Security**: Spring Security + JWT
- **APIs**: RESTful services cho story management và user progress

### Frontend - Angular
- **Framework**: Angular 17 với Standalone Components
- **UI/UX**: Responsive design với modern interface
- **Features**: Story reading, audio sync, quiz system, progress tracking

## 🚀 Cài Đặt và Chạy

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.6+
- Angular CLI

### Backend Setup

1. **Clone repository**
```bash
git clone <repository-url>
cd multi-story-app/backend
```

2. **Chạy Spring Boot application**
```bash
mvn spring-boot:run
```

3. **Truy cập H2 Console** (development)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: password
```

4. **API Documentation**
```
Base URL: http://localhost:8080/api
Health Check: http://localhost:8080/api/actuator/health
```

### Frontend Setup

1. **Cài đặt dependencies**
```bash
cd multi-story-app
npm install
```

2. **Chạy development server**
```bash
ng serve
```

3. **Truy cập ứng dụng**
```
URL: http://localhost:4200
```

## 📊 API Endpoints

### Story Management
```http
GET    /api/stories                    # Lấy danh sách stories
GET    /api/stories/{id}              # Lấy story theo ID
POST   /api/stories                   # Tạo story mới
PUT    /api/stories/{id}              # Cập nhật story
DELETE /api/stories/{id}              # Xóa story (soft delete)
GET    /api/stories/language/{lang}   # Lấy stories theo ngôn ngữ
GET    /api/stories/popular           # Lấy stories phổ biến
GET    /api/stories/search?keyword=   # Tìm kiếm stories
```

### User Progress
```http
GET    /api/progress/user/{userId}                    # Tiến độ của user
GET    /api/progress/user/{userId}/story/{storyId}    # Tiến độ cụ thể
POST   /api/progress/user/{userId}/story/{storyId}    # Cập nhật tiến độ
GET    /api/progress/user/{userId}/completed          # Stories đã hoàn thành
GET    /api/progress/user/{userId}/statistics         # Thống kê học tập
POST   /api/progress/user/{userId}/story/{storyId}/vocabulary  # Thêm từ vựng
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test                    # Chạy tất cả tests
mvn test -Dtest=StoryControllerTest  # Chạy test cụ thể
mvn jacoco:report          # Tạo coverage report
```

### Frontend Tests
```bash
ng test                    # Unit tests
ng e2e                     # End-to-end tests
ng test --code-coverage    # Coverage report
```

## 📁 Cấu Trúc Project

```
multi-story-app/
├── backend/                          # Spring Boot backend
│   ├── src/main/java/com/multistory/languageapp/
│   │   ├── entity/                   # JPA entities
│   │   ├── repository/               # Data repositories  
│   │   ├── service/                  # Business logic
│   │   ├── controller/               # REST controllers
│   │   └── dto/                      # Data transfer objects
│   ├── src/main/resources/
│   │   ├── application.yml           # Configuration
│   │   └── data.sql                  # Sample data
│   └── src/test/                     # Unit tests
├── src/                              # Angular frontend
│   ├── app/
│   │   ├── components/               # Angular components
│   │   ├── services/                 # Angular services
│   │   ├── models/                   # TypeScript interfaces
│   │   └── ...
│   └── environments/                 # Environment configs
├── architecture-design.md            # System architecture
├── business-flow-design.md           # Business workflows  
├── feature-requirements-document.md  # Requirements spec
└── executive-summary.md              # Project summary
```

## 🎯 Tính Năng Chính

### ✅ Đã Hoàn Thành
- **Story Management**: CRUD operations cho stories và chapters
- **User Progress**: Tracking tiến độ học tập chi tiết
- **Story Reader**: Đọc stories với audio sync
- **Quiz System**: Hệ thống quiz đa dạng (multiple choice, fill-in-blank, etc.)
- **Vocabulary Learning**: Interactive vocabulary với definitions
- **Responsive UI**: Giao diện thân thiện trên mobile và desktop

### 🚧 Đang Phát Triển
- AI Conversation System
- Voice Recognition & Pronunciation Assessment
- Offline Capabilities
- Social Features (leaderboards, sharing)
- Advanced Analytics

## 🛠️ Tech Stack Chi Tiết

### Backend Technologies
- **Spring Boot 3.2** - Core framework
- **Spring Data JPA** - Database operations
- **Spring Security** - Authentication & authorization
- **H2/PostgreSQL** - Database systems
- **Maven** - Dependency management
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework

### Frontend Technologies  
- **Angular 17** - Frontend framework
- **TypeScript** - Type-safe JavaScript
- **RxJS** - Reactive programming
- **Angular Material** - UI components
- **Jasmine/Karma** - Testing frameworks

## 🔧 Development Guidelines

### Code Standards
- **Java**: Google Java Style Guide
- **TypeScript**: Angular Style Guide
- **Git**: Conventional Commits
- **Testing**: Minimum 80% code coverage

### Branch Strategy
```
main           # Production branch
develop        # Development branch  
feature/*      # Feature branches
hotfix/*       # Hotfix branches
```

### API Design Principles
- RESTful design patterns
- Consistent error handling
- Input validation với Bean Validation
- Pagination cho large datasets
- CORS configuration cho cross-origin requests

## 📈 Performance Optimization

### Backend Optimizations
- Database indexing cho frequently queried fields
- JPA query optimization
- Connection pooling
- Caching với Redis (planned)

### Frontend Optimizations
- Lazy loading cho components
- OnPush change detection strategy
- Service workers cho offline support
- Audio preloading và caching

## 🔒 Security Features

### Backend Security
- JWT-based authentication
- Role-based access control
- Input validation và sanitization
- SQL injection prevention
- CORS protection

### Frontend Security
- XSS prevention
- CSRF protection
- Secure storage cho tokens
- Content Security Policy

## 📱 Deployment

### Development
```bash
# Backend
mvn spring-boot:run

# Frontend  
ng serve
```

### Production
```bash
# Backend
mvn clean package
java -jar target/language-app-backend-1.0.0.jar

# Frontend
ng build --configuration production
# Deploy build files to web server
```

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Backend Development**: Spring Boot API development
- **Frontend Development**: Angular UI/UX implementation  
- **AI Integration**: Voice recognition và conversation AI
- **DevOps**: Deployment và infrastructure

## 🔮 Roadmap

### Phase 1 (Current) - MVP
- ✅ Core story management
- ✅ Basic quiz system
- ✅ User progress tracking

### Phase 2 - AI Features
- 🚧 AI conversation system
- 🚧 Voice recognition integration
- 🚧 Pronunciation assessment

### Phase 3 - Advanced Features
- 📋 Social learning features
- 📋 Advanced analytics
- 📋 Mobile app (React Native)
- 📋 Teacher dashboard

---

**Happy Learning! 📚✨**