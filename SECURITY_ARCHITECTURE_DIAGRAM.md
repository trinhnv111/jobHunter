# 🏗️ SƠ ĐỒ KIẾN TRÚC CHI TIẾT - SPRING SECURITY FILTER CHAIN

## 🎯 TẠI SAO SECURITY CÓ THỂ BẮT EXCEPTION TRƯỚC CONTROLLER?

### ⚡ Câu trả lời ngắn gọn:

**Spring Security Filter Chain chạy TRƯỚC Controller!**

```
Request → Security Filter Chain → Controller → Response
         ↑
         └── Nếu lỗi ở đây → Security xử lý exception → Trả về response
             (Không bao giờ đến Controller)
```

---

## 📊 SƠ ĐỒ KIẾN TRÚC TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                          │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         HTTP REQUEST                                │
│                  GET /users                                          │
│                  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              SPRING SECURITY FILTER CHAIN ⚡                        │
│              (Chạy TRƯỚC Controller)                                │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Filter 1: SecurityContextPersistenceFilter                  │  │
│  │ - Khôi phục SecurityContext từ session (nếu có)              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│                              ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Filter 2: UsernamePasswordAuthenticationFilter                │  │
│  │ - Xử lý form login (đã disable trong config)                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│                              ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Filter 3: OAuth2ResourceServerJwtAuthenticationFilter ⭐     │  │
│  │ - Kiểm tra header Authorization: Bearer <token>              │  │
│  │ - Gọi JwtDecoder.decode(token)                               │  │
│  │ - Nếu token hợp lệ → Tạo Authentication object                │  │
│  │ - Nếu token KHÔNG hợp lệ → Throw AuthenticationException      │  │
│  │   → BẮT EXCEPTION Ở ĐÂY! ⚡                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│                    ┌─────────┴─────────┐                            │
│                    │                   │                            │
│              Token hợp lệ      Token KHÔNG hợp lệ                   │
│                    │                   │                            │
│                    ▼                   ▼                            │
│  ┌─────────────────────────┐  ┌──────────────────────────────┐   │
│  │ Filter 4:               │  │ EXCEPTION HANDLING ⚡        │   │
│  │ AuthorizationFilter      │  │                               │   │
│  │ - Kiểm tra quyền truy cập│  │ AuthenticationException       │   │
│  │ - authorizeHttpRequests()│  │ → CustomAuthenticationEntry   │   │
│  │   .anyRequest()          │  │   Point.commence()           │   │
│  │   .authenticated()        │  │ → Trả về 401 JSON            │   │
│  └─────────────────────────┘  │ → Response trả về client      │   │
│                              │ → KHÔNG đến Controller! ⚡      │   │
│                              └──────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│                    ┌───────────────────┐                        │
│                    │   Cho phép truy   │                        │
│                    │   cập endpoint    │                        │
│                    └───────────────────┘                        │
└──────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    DISPATCHER SERVLET                                │
│              (Router của Spring MVC)                                 │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                               │
│              (Chỉ chạy nếu Security cho phép)                       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ UserController.getAllUsers()                                 │  │
│  │ - Xử lý business logic                                      │  │
│  │ - Gọi UserService                                             │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│                              ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ SERVICE LAYER                                                │  │
│  │ UserService.getAllUsers()                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│                              ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ REPOSITORY LAYER                                             │  │
│  │ UserRepository.findAll()                                     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         HTTP RESPONSE                               │
│                 200 OK                                               │
│                 { "data": [...] }                                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 CHI TIẾT: TẠI SAO SECURITY BẮT ĐƯỢC EXCEPTION?

### 1. Spring Security Filter Chain là gì?

**Filter Chain** là một chuỗi các Filter được chạy **TRƯỚC** khi request đến Controller.

```
Request → Filter 1 → Filter 2 → Filter 3 → ... → Controller
```

**Đặc điểm quan trọng**:
- Filter chạy **TRƯỚC** DispatcherServlet (router của Spring MVC)
- Filter có thể **CHẶN** request và trả về response ngay lập tức
- Filter có thể **XỬ LÝ EXCEPTION** và trả về response

---

### 2. Luồng xử lý request chi tiết

```
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 1: HTTP REQUEST ĐẾN SERVER                                 │
└─────────────────────────────────────────────────────────────────┘

    GET /users
    Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
    
    ↓
    
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 2: SPRING SECURITY FILTER CHAIN BẮT ĐẦU XỬ LÝ            │
│         (Chạy TRƯỚC DispatcherServlet)                         │
└─────────────────────────────────────────────────────────────────┘

    Filter Chain:
    
    ┌─────────────────────────────────────────────────────────┐
    │ Filter: OAuth2ResourceServerJwtAuthenticationFilter     │
    │                                                          │
    │ 1. Lấy token từ header:                                  │
    │    Authorization: Bearer <token>                         │
    │                                                          │
    │ 2. Gọi JwtDecoder.decode(token):                         │
    │    - Kiểm tra signature                                  │
    │    - Kiểm tra thời gian hết hạn                          │
    │                                                          │
    │ 3. Nếu token hợp lệ:                                     │
    │    → Tạo Authentication object                           │
    │    → Lưu vào SecurityContext                            │
    │    → Cho phép request tiếp tục                           │
    │                                                          │
    │ 4. Nếu token KHÔNG hợp lệ: ⚡                            │
    │    → Throw AuthenticationException                       │
    │    → EXCEPTION ĐƯỢC BẮT Ở ĐÂY!                           │
    └─────────────────────────────────────────────────────────┘
    
    ↓ (Nếu exception)
    
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 3: EXCEPTION HANDLING TRONG SECURITY                      │
│         (KHÔNG BAO GIỜ ĐẾN CONTROLLER)                         │
└─────────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────────┐
    │ Security Filter Chain catch exception:                  │
    │                                                          │
    │ AuthenticationException                                 │
    │   → Gọi exceptionHandling()                              │
    │   → Tìm AuthenticationEntryPoint                         │
    │   → Tìm thấy CustomAuthenticationEntryPoint              │
    │   → Gọi commence(request, response, exception)          │
    └─────────────────────────────────────────────────────────┘
    
    ↓
    
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 4: CustomAuthenticationEntryPoint XỬ LÝ                   │
└─────────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────────┐
    │ CustomAuthenticationEntryPoint.commence():              │
    │                                                          │
    │ 1. Set HTTP status code 401                             │
    │ 2. Set WWW-Authenticate header                          │
    │ 3. Tạo ApiResponse với thông tin lỗi                     │
    │ 4. Convert thành JSON                                   │
    │ 5. Ghi vào response.getWriter()                          │
    │ 6. Response được gửi về client                          │
    │                                                          │
    │ → REQUEST KẾT THÚC Ở ĐÂY!                                │
    │ → KHÔNG BAO GIỜ ĐẾN CONTROLLER! ⚡                        │
    └─────────────────────────────────────────────────────────┘
    
    ↓
    
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 5: HTTP RESPONSE TRẢ VỀ CLIENT                            │
└─────────────────────────────────────────────────────────────────┘

    401 Unauthorized
    {
      "statusCode": 401,
      "error": "JWT expired at...",
      "message": "Token hết hạn,không hợp lệ,........."
    }
```

---

## 🔄 SO SÁNH: SECURITY EXCEPTION vs CONTROLLER EXCEPTION

### Security Exception Handling (CustomAuthenticationEntryPoint)

```
Request → Security Filter Chain
         → JwtDecoder.decode() throw exception
         → Security catch exception
         → CustomAuthenticationEntryPoint.commence()
         → Response 401
         → KHÔNG đến Controller
```

**Khi nào xảy ra?**
- Token không tồn tại
- Token không hợp lệ
- Token hết hạn
- Token format sai

**Xử lý ở đâu?**
- `config/CustomAuthenticationEntryPoint.java`
- Được đăng ký trong `SecurityConfiguration.securityFilterChain()`

---

### Controller Exception Handling (GlobalException)

```
Request → Security Filter Chain (pass)
         → DispatcherServlet
         → Controller
         → Business logic throw exception
         → GlobalException.handleException()
         → Response 400/500
```

**Khi nào xảy ra?**
- BadCredentialsException (username/password sai)
- UsernameNotFoundException (user không tồn tại)
- MethodArgumentNotValidException (validation fail)
- IdInvalidException (ID không hợp lệ)

**Xử lý ở đâu?**
- `util/error/GlobalException.java`
- `@ControllerAdvice` annotation

---

## 📊 SƠ ĐỒ SO SÁNH CHI TIẾT

```
┌─────────────────────────────────────────────────────────────────┐
│                    REQUEST ĐẾN SERVER                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              SPRING SECURITY FILTER CHAIN                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ OAuth2ResourceServerJwtAuthenticationFilter              │  │
│  │                                                           │  │
│  │ JwtDecoder.decode(token)                                  │  │
│  │   ↓                                                       │  │
│  │   ├─ Token hợp lệ → Authentication object                │  │
│  │   │                                                       │  │
│  │   └─ Token KHÔNG hợp lệ → Throw AuthenticationException  │  │
│  │       ↓                                                   │  │
│  │       └─→ CustomAuthenticationEntryPoint.commence() ⚡    │  │
│  │           ↓                                               │  │
│  │           └─→ Response 401 JSON                           │  │
│  │               └─→ KẾT THÚC (không đến Controller)         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                    Token hợp lệ
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DISPATCHER SERVLET                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER                                  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AuthController.login()                                   │  │
│  │   ↓                                                       │  │
│  │   ├─ Validation fail → MethodArgumentNotValidException    │  │
│  │   │   ↓                                                   │  │
│  │   │   └─→ GlobalException.handleMethodArgumentNotValid()  │  │
│  │   │       ↓                                               │  │
│  │   │       └─→ Response 400 JSON                          │  │
│  │   │                                                       │  │
│  │   └─ Authentication fail → BadCredentialsException       │  │
│  │       ↓                                                   │  │
│  │       └─→ GlobalException.handleUserPrincipalNotFound()   │  │
│  │           ↓                                               │  │
│  │           └─→ Response 400 JSON                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 TÓM TẮT: TẠI SAO SECURITY BẮT ĐƯỢC EXCEPTION?

### 1. **Filter Chain chạy TRƯỚC Controller**

```
Request → Filter Chain → DispatcherServlet → Controller
         ↑
         └── Nếu exception ở đây → Security xử lý → Response
             (Không bao giờ đến Controller)
```

### 2. **Security có thể CHẶN request**

- Security Filter Chain có thể **CHẶN** request và trả về response ngay lập tức
- Không cần đến Controller

### 3. **Exception được xử lý trong Filter Chain**

- Exception xảy ra trong Filter Chain
- Security catch exception và gọi `AuthenticationEntryPoint`
- `CustomAuthenticationEntryPoint.commence()` xử lý và trả về response

### 4. **Response được gửi trực tiếp**

- Response được ghi vào `HttpServletResponse`
- Request kết thúc ở đây
- Không bao giờ đến Controller

---

## 🔧 CODE MINH HỌA

### SecurityConfiguration.java

```java
@Bean
public SecurityFilterChain securityFilterChain(...) {
    http
        .oauth2ResourceServer((oauth2) -> oauth2
            .jwt(Customizer.withDefaults())
            // ⭐ Đăng ký CustomAuthenticationEntryPoint
            .authenticationEntryPoint(customAuthenticationEntryPoint)
        )
        .exceptionHandling(
            exceptions -> exceptions
                // ⭐ Xử lý exception 401 (token không hợp lệ)
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                // ⭐ Xử lý exception 403 (không có quyền)
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
        );
}
```

### CustomAuthenticationEntryPoint.java

```java
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(...) {
        // ⭐ Method này được gọi KHI:
        // - Token không tồn tại
        // - Token không hợp lệ
        // - Token hết hạn
        // 
        // ⭐ Được gọi TRONG Security Filter Chain
        // ⭐ KHÔNG BAO GIỜ đến Controller
        
        // Set HTTP status code 401
        // Tạo ApiResponse với thông tin lỗi
        // Convert thành JSON và ghi vào response
        // Response được gửi về client
    }
}
```

---

## 📝 KẾT LUẬN

**Tại sao Security có thể bắt exception?**

1. ✅ **Filter Chain chạy TRƯỚC Controller**
2. ✅ **Security có thể CHẶN request và trả về response**
3. ✅ **Exception được xử lý trong Filter Chain**
4. ✅ **Response được gửi trực tiếp, không đến Controller**

**Điểm quan trọng**:
- Security Exception Handling: Xử lý lỗi **TRƯỚC** khi đến Controller
- Controller Exception Handling: Xử lý lỗi **SAU** khi đến Controller
- Hai cơ chế này **BỔ SUNG** cho nhau, không xung đột!

