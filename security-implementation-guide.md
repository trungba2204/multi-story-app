# Security Implementation Guide - Multi-Story Language App

## 🔐 1. Authentication & Authorization Implementation

### 1.1 JWT Configuration

```java
// SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/stories/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stories/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Dev only
                
                // Protected endpoints
                .requestMatchers("/api/progress/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/stories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/stories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/stories/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 1.2 JWT Token Service

```java
// JwtTokenUtil.java
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration; // in seconds
    
    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;
    
    // Generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }
    
    // Generate refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        return createRefreshToken(userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(getSignKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    private String createRefreshToken(String subject) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration * 1000))
            .signWith(getSignKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
```

### 1.3 Authentication Controller

```java
// AuthController.java
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), 
                    loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials", e);
        }
        
        final UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(new JwtResponse(token, refreshToken, userDetails.getUsername()));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use!"));
        }
        
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setName(registerRequest.getName());
        user.setRole(Role.USER);
        
        User savedUser = userService.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        try {
            String username = jwtTokenUtil.extractUsername(refreshToken);
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                String newToken = jwtTokenUtil.generateToken(userDetails);
                return ResponseEntity.ok(new JwtResponse(newToken, refreshToken, username));
            }
        } catch (Exception e) {
            log.error("Cannot refresh token", e);
        }
        
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid refresh token"));
    }
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // Add token to blacklist or invalidate in Redis
        String jwtToken = token.substring(7);
        tokenBlacklistService.blacklistToken(jwtToken);
        
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
```

## 🛡️ 2. Data Security Implementation

### 2.1 Input Validation & Sanitization

```java
// InputSanitizer.java
@Component
public class InputSanitizer {
    
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("b", "i", "em", "strong")
        .toFactory();
    
    public String sanitizeHtml(String input) {
        if (input == null) return null;
        return POLICY.sanitize(input);
    }
    
    public String sanitizeSql(String input) {
        if (input == null) return null;
        // Remove SQL meta characters
        return input.replaceAll("['\"\\\\;]", "");
    }
}

// StoryController.java - Updated with validation
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<StoryDTO> createStory(@Valid @RequestBody StoryCreateRequest request) {
    // Sanitize input
    request.setTitle(inputSanitizer.sanitizeHtml(request.getTitle()));
    request.setContent(inputSanitizer.sanitizeHtml(request.getContent()));
    
    StoryDTO createdStory = storyService.createStory(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdStory);
}
```

### 2.2 Encryption for Sensitive Data

```java
// EncryptionService.java
@Service
public class EncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    public String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        SecretKeySpec keySpec = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES");
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] encrypted = new byte[iv.length + cipherText.length];
        
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);
        
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String decrypt(String encryptedText) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encryptedText);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherText = new byte[encrypted.length - GCM_IV_LENGTH];
        
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);
        
        SecretKeySpec keySpec = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES");
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        
        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText, StandardCharsets.UTF_8);
    }
}
```

### 2.3 Secure Headers Configuration

```java
// SecurityHeadersFilter.java
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self' https://api.multistoryapp.com");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", 
            "accelerometer=(), camera=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), payment=(), usb=()");
        
        chain.doFilter(request, response);
    }
}
```

## 🔒 3. API Security Best Practices

### 3.1 Rate Limiting Implementation

```java
// RateLimitingConfig.java
@Configuration
public class RateLimitingConfig {
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 1); // 100 requests per second
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getHeaders().getFirst("X-User-Id") != null ?
            exchange.getRequest().getHeaders().getFirst("X-User-Id") :
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}

// RateLimitInterceptor.java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String key = getClientKey(request);
        RateLimiter rateLimiter = limiters.computeIfAbsent(key, 
            k -> RateLimiter.create(getPermitsPerSecond(request)));
        
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }
        
        return true;
    }
    
    private String getClientKey(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + request.getRemoteAddr();
    }
    
    private double getPermitsPerSecond(HttpServletRequest request) {
        // Different limits for different endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            return 5.0; // 5 requests per second for auth
        } else if (path.startsWith("/api/stories")) {
            return 100.0; // 100 requests per second for stories
        }
        return 50.0; // Default
    }
}
```

### 3.2 API Versioning & Deprecation

```java
// ApiVersionInterceptor.java
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String apiVersion = request.getHeader("API-Version");
        
        if (apiVersion == null || apiVersion.isEmpty()) {
            apiVersion = "1.0"; // Default version
        }
        
        // Check if version is supported
        if (!isSupportedVersion(apiVersion)) {
            response.setStatus(400);
            response.getWriter().write("API version " + apiVersion + " is not supported");
            return false;
        }
        
        // Add deprecation warning for old versions
        if (isDeprecatedVersion(apiVersion)) {
            response.addHeader("X-API-Deprecation-Warning", 
                "This API version is deprecated and will be removed on 2025-01-01");
        }
        
        request.setAttribute("apiVersion", apiVersion);
        return true;
    }
    
    private boolean isSupportedVersion(String version) {
        return Arrays.asList("1.0", "1.1", "2.0").contains(version);
    }
    
    private boolean isDeprecatedVersion(String version) {
        return "1.0".equals(version);
    }
}
```

## 🎯 4. Security Testing Checklist

### OWASP Top 10 Coverage

```yaml
1. Injection:
   ✓ Parameterized queries
   ✓ Input validation
   ✓ Stored procedure usage
   
2. Broken Authentication:
   ✓ Strong password policy
   ✓ Account lockout mechanism
   ✓ Session timeout
   
3. Sensitive Data Exposure:
   ✓ HTTPS enforcement
   ✓ Data encryption at rest
   ✓ No sensitive data in logs
   
4. XML External Entities (XXE):
   ✓ Disable XML external entity processing
   ✓ Use JSON instead of XML
   
5. Broken Access Control:
   ✓ Role-based access control
   ✓ Default deny policy
   ✓ Access control checks
   
6. Security Misconfiguration:
   ✓ Security headers configured
   ✓ Error handling doesn't expose stack traces
   ✓ Latest security patches
   
7. Cross-Site Scripting (XSS):
   ✓ Input sanitization
   ✓ Output encoding
   ✓ Content Security Policy
   
8. Insecure Deserialization:
   ✓ Input validation for serialized objects
   ✓ Type checking
   
9. Using Components with Known Vulnerabilities:
   ✓ Dependency scanning
   ✓ Regular updates
   
10. Insufficient Logging & Monitoring:
    ✓ Security event logging
    ✓ Log monitoring
    ✓ Alerting system
```

### Security Test Automation

```java
// SecurityTest.java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/progress/user/1"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void testSQLInjection() throws Exception {
        String maliciousInput = "'; DROP TABLE stories; --";
        
        mockMvc.perform(get("/api/stories/search")
            .param("keyword", maliciousInput))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
        
        // Verify table still exists
        mockMvc.perform(get("/api/stories"))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testXSSPrevention() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        
        StoryCreateRequest request = new StoryCreateRequest();
        request.setTitle(xssPayload);
        request.setContent("Test content");
        request.setLanguage("en");
        request.setDifficulty("BEGINNER");
        
        MvcResult result = mockMvc.perform(post("/api/stories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + getAdminToken()))
            .andExpect(status().isCreated())
            .andReturn();
        
        StoryDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(), StoryDTO.class);
        
        assertFalse(response.getTitle().contains("<script>"));
    }
}
```

## 🔐 5. Production Security Checklist

### Pre-Deployment:
- [ ] All endpoints require authentication (except public ones)
- [ ] JWT secret is strong and stored securely
- [ ] Database credentials are encrypted
- [ ] HTTPS is enforced
- [ ] Security headers are configured
- [ ] Input validation on all endpoints
- [ ] Rate limiting is enabled
- [ ] CORS is properly configured
- [ ] Error messages don't expose sensitive info
- [ ] Logging doesn't contain sensitive data

### Post-Deployment:
- [ ] SSL certificate is valid
- [ ] Security monitoring is active
- [ ] Intrusion detection is configured
- [ ] Regular security scans scheduled
- [ ] Incident response plan in place
- [ ] Backup encryption verified
- [ ] Access logs are monitored
- [ ] Failed login attempts are tracked
- [ ] API usage is monitored
- [ ] Security patches are up to date

---

**Important**: Review and update security measures regularly. Security is an ongoing process, not a one-time implementation.
