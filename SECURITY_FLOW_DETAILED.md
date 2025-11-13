# 🔄 SƠ ĐỒ LUỒNG CHI TIẾT - SECURITY FILTER CHAIN

## 🎯 TẠI SAO SECURITY BẮT ĐƯỢC EXCEPTION TRƯỚC CONTROLLER?

### Câu trả lời: **Filter Chain chạy TRƯỚC DispatcherServlet!**

---

## 📊 SƠ ĐỒ LUỒNG REQUEST CHI TIẾT

### Scenario 1: Request với token KHÔNG hợp lệ

```
╔═══════════════════════════════════════════════════════════════════╗
║                    CLIENT GỬI REQUEST                             ║
╚═══════════════════════════════════════════════════════════════════╝

    GET /users
    Authorization: Bearer eyJhbGciOiJIUzUxMiJ9... (TOKEN HẾT HẠN)
    
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║         SPRING BOOT APPLICATION START                              ║
║         (Tomcat Server nhận request)                             ║
╚═══════════════════════════════════════════════════════════════════╝
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║    ⭐ BƯỚC 1: SPRING SECURITY FILTER CHAIN                        ║
║    ⭐ CHẠY TRƯỚC DispatcherServlet!                                ║
╚═══════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────┐
    │ Filter: SecurityContextPersistenceFilter                    │
    │ - Khôi phục SecurityContext từ session (nếu có)            │
    │ - Không có session (stateless) → Bỏ qua                    │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
    ┌─────────────────────────────────────────────────────────────┐
    │ Filter: OAuth2ResourceServerJwtAuthenticationFilter ⭐    │
    │                                                              │
    │ 1. Kiểm tra header Authorization:                            │
    │    ✅ Có header: Authorization: Bearer <token>              │
    │                                                              │
    │ 2. Lấy token từ header:                                     │
    │    token = "eyJhbGciOiJIUzUxMiJ9..."                        │
    │                                                              │
    │ 3. Gọi JwtDecoder.decode(token):                            │
    │    ┌────────────────────────────────────────────────────┐    │
    │    │ JwtDecoder.decode()                               │    │
    │    │                                                   │    │
    │    │ a) Giải mã Base64URL header                       │    │
    │    │ b) Giải mã Base64URL payload                      │    │
    │    │ c) Kiểm tra signature bằng secret key             │    │
    │    │    ✅ Signature hợp lệ                            │    │
    │    │ d) Kiểm tra thời gian hết hạn (expiresAt)         │    │
    │    │    ❌ Token đã hết hạn!                            │    │
    │    │    → Throw ExpiredJwtException ⚡                  │    │
    │    └────────────────────────────────────────────────────┘    │
    │                                                              │
    │ 4. Exception được throw:                                    │
    │    ExpiredJwtException: "JWT expired at 2024-01-01..."      │
    │                                                              │
    │ 5. Security Filter Chain catch exception:                  │
    │    → Không cho request tiếp tục                             │
    │    → Gọi exceptionHandling()                                │
    │    → Tìm AuthenticationEntryPoint                           │
    │    → Tìm thấy CustomAuthenticationEntryPoint                │
    │    → Gọi commence(request, response, exception) ⚡          │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║    ⭐ BƯỚC 2: CUSTOM AUTHENTICATION ENTRY POINT                   ║
║    ⭐ XỬ LÝ EXCEPTION VÀ TRẢ VỀ RESPONSE                           ║
║    ⭐ KHÔNG BAO GIỜ ĐẾN CONTROLLER!                               ║
╚═══════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────┐
    │ CustomAuthenticationEntryPoint.commence()                  │
    │                                                              │
    │ 1. Gọi default handler:                                      │
    │    BearerTokenAuthenticationEntryPoint.commence()           │
    │    → Set HTTP status code 401                               │
    │    → Set header: WWW-Authenticate: Bearer                   │
    │                                                              │
    │ 2. Set response content type:                               │
    │    Content-Type: application/json;charset=UTF-8             │
    │                                                              │
    │ 3. Tạo ApiResponse object:                                  │
    │    {                                                         │
    │      "statusCode": 401,                                      │
    │      "error": "JWT expired at 2024-01-01T00:00:00Z",        │
    │      "message": "Token hết hạn,không hợp lệ,........."       │
    │    }                                                         │
    │                                                              │
    │ 4. Convert thành JSON và ghi vào response:                  │
    │    objectMapper.writeValue(response.getWriter(), apiResponse)│
    │                                                              │
    │ 5. Response được gửi về client:                             │
    │    → REQUEST KẾT THÚC Ở ĐÂY! ⚡                              │
    │    → KHÔNG BAO GIỜ ĐẾN CONTROLLER! ⚡                        │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║                    HTTP RESPONSE TRẢ VỀ CLIENT                    ║
╚═══════════════════════════════════════════════════════════════════╝

    401 Unauthorized
    Content-Type: application/json;charset=UTF-8
    WWW-Authenticate: Bearer
    
    {
      "statusCode": 401,
      "error": "JWT expired at 2024-01-01T00:00:00Z",
      "message": "Token hết hạn,không hợp lệ,.........",
      "data": null
    }
```

---

### Scenario 2: Request với token hợp lệ

```
╔═══════════════════════════════════════════════════════════════════╗
║                    CLIENT GỬI REQUEST                             ║
╚═══════════════════════════════════════════════════════════════════╝

    GET /users
    Authorization: Bearer eyJhbGciOiJIUzUxMiJ9... (TOKEN HỢP LỆ)
    
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║    ⭐ BƯỚC 1: SPRING SECURITY FILTER CHAIN                        ║
╚═══════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────┐
    │ Filter: OAuth2ResourceServerJwtAuthenticationFilter        │
    │                                                              │
    │ 1. Lấy token từ header                                      │
    │ 2. Gọi JwtDecoder.decode(token)                             │
    │    ✅ Token hợp lệ                                           │
    │    ✅ Signature đúng                                        │
    │    ✅ Chưa hết hạn                                           │
    │    → Tạo Authentication object                               │
    │    → Lưu vào SecurityContext                                │
    │                                                              │
    │ 3. Cho phép request tiếp tục ✅                              │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
    ┌─────────────────────────────────────────────────────────────┐
    │ Filter: AuthorizationFilter                                │
    │                                                              │
    │ 1. Kiểm tra authorizeHttpRequests:                          │
    │    - requestMatchers("/","/login").permitAll()              │
    │    - anyRequest().authenticated()                            │
    │                                                              │
    │ 2. Request là /users → Cần authenticated                    │
    │ 3. Kiểm tra SecurityContext có Authentication không:        │
    │    ✅ Có Authentication (từ JwtDecoder)                      │
    │    → Cho phép truy cập ✅                                    │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║    ⭐ BƯỚC 2: DISPATCHER SERVLET                                  ║
║    ⭐ Router của Spring MVC                                       ║
╚═══════════════════════════════════════════════════════════════════╝

    DispatcherServlet:
    - Tìm Controller phù hợp với URL: /users
    - Tìm thấy: UserController.getAllUsers()
    - Gọi method getAllUsers()
    
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║    ⭐ BƯỚC 3: CONTROLLER LAYER                                    ║
╚═══════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────┐
    │ UserController.getAllUsers()                               │
    │                                                              │
    │ 1. Gọi UserService.getAllUsers()                            │
    │ 2. UserService gọi UserRepository.findAll()                 │
    │ 3. UserRepository query database                            │
    │ 4. Trả về danh sách users                                    │
    │ 5. Controller trả về ResponseEntity                         │
    └─────────────────────────────────────────────────────────────┘
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║                    HTTP RESPONSE TRẢ VỀ CLIENT                    ║
╚═══════════════════════════════════════════════════════════════════╝

    200 OK
    {
      "data": [
        { "id": 1, "userName": "trinhnv", ... },
        { "id": 2, "userName": "user2", ... }
      ],
      "message": "CALL API SUCCEEDED"
    }
```

---

## 🔍 SO SÁNH: SECURITY vs CONTROLLER EXCEPTION HANDLING

### Bảng so sánh

| Tiêu chí | Security Exception Handling | Controller Exception Handling |
|----------|----------------------------|-------------------------------|
| **Vị trí** | Filter Chain (TRƯỚC DispatcherServlet) | Controller Layer (SAU DispatcherServlet) |
| **Khi nào chạy** | Khi token không hợp lệ | Khi business logic throw exception |
| **Xử lý ở đâu** | `CustomAuthenticationEntryPoint` | `GlobalException` (@ControllerAdvice) |
| **Có đến Controller không?** | ❌ KHÔNG | ✅ CÓ |
| **Loại exception** | AuthenticationException, AccessDeniedException | BadCredentialsException, ValidationException, ... |
| **HTTP Status** | 401 (Unauthorized), 403 (Forbidden) | 400 (Bad Request), 500 (Internal Server Error) |

---

### Sơ đồ so sánh

```
┌─────────────────────────────────────────────────────────────────┐
│                    REQUEST ĐẾN SERVER                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│         SPRING SECURITY FILTER CHAIN                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ OAuth2ResourceServerJwtAuthenticationFilter              │ │
│  │                                                           │ │
│  │ JwtDecoder.decode(token)                                  │ │
│  │   ├─ Token hợp lệ → Cho phép tiếp tục                    │ │
│  │   │                                                       │ │
│  │   └─ Token KHÔNG hợp lệ → Throw AuthenticationException  │ │
│  │       ↓                                                   │ │
│  │       └─→ CustomAuthenticationEntryPoint.commence() ⚡   │ │
│  │           ↓                                               │ │
│  │           └─→ Response 401 JSON                           │ │
│  │               └─→ KẾT THÚC (không đến Controller)        │ │
│  └──────────────────────────────────────────────────────────┘ │
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
│                      CONTROLLER LAYER                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AuthController.login()                                  │  │
│  │                                                          │  │
│  │   ├─ Validation fail → MethodArgumentNotValidException   │  │
│  │   │   ↓                                                  │  │
│  │   │   └─→ GlobalException.handleMethodArgumentNotValid() │  │
│  │   │       ↓                                              │  │
│  │   │       └─→ Response 400 JSON                         │  │
│  │   │                                                       │  │
│  │   └─ Authentication fail → BadCredentialsException       │  │
│  │       ↓                                                  │  │
│  │       └─→ GlobalException.handleUserPrincipalNotFound() │  │
│  │           ↓                                              │  │
│  │           └─→ Response 400 JSON                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 TÓM TẮT: TẠI SAO SECURITY BẮT ĐƯỢC EXCEPTION?

### 1. **Filter Chain chạy TRƯỚC DispatcherServlet**

```
Request → Filter Chain → DispatcherServlet → Controller
         ↑
         └── Security Filter Chain chạy TRƯỚC
             → Có thể CHẶN request và trả về response
             → Không bao giờ đến Controller
```

### 2. **Security có thể CHẶN request**

- Security Filter Chain có thể **CHẶN** request ngay tại Filter
- Không cần đến DispatcherServlet hay Controller
- Response được ghi trực tiếp vào `HttpServletResponse`

### 3. **Exception được xử lý trong Filter Chain**

- Exception xảy ra trong Filter Chain (JwtDecoder.decode())
- Security catch exception và gọi `exceptionHandling()`
- `CustomAuthenticationEntryPoint.commence()` xử lý và trả về response

### 4. **Response được gửi trực tiếp**

- Response được ghi vào `HttpServletResponse.getWriter()`
- Request kết thúc ở đây
- Không bao giờ đến Controller

---

## 📝 CODE MINH HỌA

### SecurityConfiguration.java

```java
@Bean
public SecurityFilterChain securityFilterChain(...) {
    http
        // ⭐ Cấu hình OAuth2 Resource Server với JWT
        .oauth2ResourceServer((oauth2) -> oauth2
            .jwt(Customizer.withDefaults())
            // ⭐ Đăng ký CustomAuthenticationEntryPoint
            // → Được gọi KHI token không hợp lệ
            // → Chạy TRƯỚC Controller
            .authenticationEntryPoint(customAuthenticationEntryPoint)
        )
        // ⭐ Xử lý exception trong Security Filter Chain
        .exceptionHandling(
            exceptions -> exceptions
                // 401: Token không hợp lệ
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                // 403: Token hợp lệ nhưng không có quyền
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
        // ⭐ Method này được gọi TRONG Security Filter Chain
        // ⭐ KHÔNG BAO GIỜ đến Controller
        
        // 1. Set HTTP status code 401
        // 2. Tạo ApiResponse với thông tin lỗi
        // 3. Convert thành JSON
        // 4. Ghi vào response.getWriter()
        // 5. Response được gửi về client
        // → Request kết thúc ở đây!
    }
}
```

---

## ✅ KẾT LUẬN

**Tại sao Security có thể bắt exception trước Controller?**

1. ✅ **Filter Chain chạy TRƯỚC DispatcherServlet**
2. ✅ **Security có thể CHẶN request và trả về response**
3. ✅ **Exception được xử lý trong Filter Chain**
4. ✅ **Response được gửi trực tiếp, không đến Controller**

**Điểm quan trọng**:
- Security Exception Handling: Xử lý lỗi **TRƯỚC** khi đến Controller
- Controller Exception Handling: Xử lý lỗi **SAU** khi đến Controller
- Hai cơ chế này **BỔ SUNG** cho nhau, không xung đột!

