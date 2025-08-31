# Performance Test Plan - Multi-Story Language App

## 🎯 Test Objectives
Đảm bảo hệ thống có thể xử lý 1,000 concurrent users với performance chấp nhận được.

## 📊 Test Scenarios

### Scenario 1: User Journey Simulation
```yaml
Virtual Users: 1,000
Ramp-up Time: 10 minutes
Test Duration: 60 minutes
User Flow:
  1. Login (10%)
  2. Browse stories (30%) 
  3. Read story + audio (40%)
  4. Take quiz (15%)
  5. Update progress (5%)
```

### Scenario 2: API Stress Test
```yaml
Endpoints to Test:
  - GET /api/stories (500 req/sec)
  - GET /api/stories/{id} (300 req/sec)
  - POST /api/progress/update (200 req/sec)
  - GET /api/audio/stream (500 concurrent)
```

## 🛠️ JMeter Test Configuration

### Thread Group Setup
```xml
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">1000</stringProp>
  <stringProp name="ThreadGroup.ramp_time">600</stringProp>
  <stringProp name="ThreadGroup.duration">3600</stringProp>
  <boolProp name="ThreadGroup.scheduler">true</boolProp>
</ThreadGroup>
```

### HTTP Request Samplers
```xml
<!-- Browse Stories -->
<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.path">/api/stories?page=0&size=20</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSamplerProxy>

<!-- Read Story -->
<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.path">/api/stories/${story_id}</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSamplerProxy>

<!-- Stream Audio -->
<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.path">/api/audio/stream/${audio_id}</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
  <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
</HTTPSamplerProxy>
```

### Assertions
```xml
<ResponseAssertion>
  <collectionProp name="Asserion.test_strings">
    <stringProp name="49586">200</stringProp>
  </collectionProp>
  <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
</ResponseAssertion>

<DurationAssertion>
  <stringProp name="DurationAssertion.duration">2000</stringProp>
</DurationAssertion>
```

## 📈 Gatling Test Script

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MultiStoryLoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("Multi-Story-App-Test")
  
  val browseStories = exec(
    http("Browse Stories")
      .get("/api/stories")
      .queryParam("page", "0")
      .queryParam("size", "20")
      .check(status.is(200))
      .check(responseTimeInMillis.lt(2000))
  )
  
  val readStory = exec(
    http("Read Story")
      .get("/api/stories/#{storyId}")
      .check(status.is(200))
      .check(jsonPath("$.id").exists)
  )
  
  val streamAudio = exec(
    http("Stream Audio")
      .get("/api/audio/stream/#{audioId}")
      .check(status.is(200))
  ).pause(30) // Simulate audio listening
  
  val takeQuiz = exec(
    http("Submit Quiz")
      .post("/api/quiz/submit")
      .body(StringBody("""{"answers": [{"questionId": 1, "answer": "A"}]}"""))
      .asJson
      .check(status.is(200))
  )
  
  val userScenario = scenario("User Learning Flow")
    .exec(browseStories)
    .pause(2)
    .exec(readStory)
    .pause(5)
    .exec(streamAudio)
    .pause(2)
    .exec(takeQuiz)
  
  setUp(
    userScenario.inject(
      rampUsers(1000) during (10 minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(5000),
     global.responseTime.percentile(95).lt(2000),
     global.successfulRequests.percent.gt(99)
   )
}
```

## 🔍 Monitoring Setup

### Application Metrics (Micrometer + Prometheus)
```java
@RestController
public class StoryController {
    private final MeterRegistry meterRegistry;
    
    @GetMapping("/stories")
    @Timed(value = "stories.get.all", percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<Page<StoryDTO>> getAllStories() {
        return meterRegistry.timer("stories.get.all").record(() -> {
            return storyService.getStoriesWithFilters(...);
        });
    }
}
```

### Database Monitoring
```sql
-- Connection pool monitoring
SELECT 
    count(*) as active_connections,
    state,
    wait_event_type
FROM pg_stat_activity
WHERE datname = 'multistory_db'
GROUP BY state, wait_event_type;

-- Slow query log
SELECT 
    query,
    mean_exec_time,
    calls
FROM pg_stat_statements
WHERE mean_exec_time > 1000 -- queries > 1 second
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### Infrastructure Metrics
```yaml
# Prometheus queries
- CPU Usage: rate(process_cpu_seconds_total[5m]) * 100
- Memory Usage: jvm_memory_used_bytes / jvm_memory_max_bytes
- Request Rate: rate(http_server_requests_seconds_count[5m])
- Error Rate: rate(http_server_requests_seconds_count{status=~"5.."}[5m])
- Response Time: histogram_quantile(0.95, http_server_requests_seconds_bucket)
```

## 📋 Test Execution Checklist

### Pre-Test:
- [ ] Production-like environment setup
- [ ] Database populated with realistic data (1000+ stories)
- [ ] Monitoring tools configured
- [ ] Baseline performance recorded
- [ ] Test data prepared (user accounts, etc.)

### During Test:
- [ ] Monitor error rates in real-time
- [ ] Watch for memory leaks
- [ ] Check database connection pool
- [ ] Monitor disk I/O for audio streaming
- [ ] Track network bandwidth usage

### Post-Test:
- [ ] Analyze response time distribution
- [ ] Identify bottlenecks from metrics
- [ ] Review error logs
- [ ] Generate performance report
- [ ] Plan optimization based on findings

## 🎯 Success Criteria

```yaml
Response Times:
  - Browse Stories: p95 < 1s, p99 < 2s
  - Read Story: p95 < 1.5s, p99 < 3s
  - Audio Stream Start: p95 < 2s
  - Quiz Submit: p95 < 1s, p99 < 2s

System Metrics:
  - CPU Usage: < 70% average
  - Memory Usage: < 80% of allocated
  - Error Rate: < 0.5%
  - Throughput: > 1000 req/sec combined

User Experience:
  - Page Load Time: < 3s
  - Time to Interactive: < 5s
  - Audio Buffer Time: < 2s
  - Zero audio interruptions
```

## 🔧 Optimization Recommendations Based on Test Results

### If Response Times Are High:
1. Implement Redis caching
2. Add database read replicas
3. Optimize N+1 queries
4. Enable HTTP/2

### If Memory Usage Is High:
1. Tune JVM heap settings
2. Implement pagination properly
3. Stream large responses
4. Fix memory leaks

### If Error Rate Is High:
1. Increase connection pool size
2. Implement circuit breakers
3. Add retry logic
4. Scale horizontally

---

**Note**: Run tests during off-peak hours first, then gradually increase to peak-hour simulations.
