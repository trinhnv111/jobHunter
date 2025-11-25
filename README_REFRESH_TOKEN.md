# 🔐 JWT Authentication - STATELESS

## Mục lục
- [Tổng quan](#tổng-quan)
- [Stateless là gì?](#stateless-là-gì)
- [Kiến trúc](#kiến-trúc)
- [API Endpoints](#api-endpoints)
- [JWT Token Structure](#jwt-token-structure)
- [Flow Diagrams](#flow-diagrams)
- [Cấu hình](#cấu-hình)
- [Trade-offs](#trade-offs)
- [Best Practices](#best-practices)

---

## Tổng quan

Hệ thống authentication sử dụng **JWT (JSON Web Token)** theo hướng **STATELESS**.

### So sánh Access Token vs Refresh Token

| Feature | Access Token | Refresh Token |
|---------|--------------|---------------|
| **Format** | JWT | JWT |
| **Lifetime** | 15 phút | 7 ngày |
| **Storage** | Client memory | Client storage |
| **Purpose** | Access protected resources | Lấy access token mới |
| **Lưu DB?** | ❌ Không | ❌ Không |

---

## Stateless là gì?

```
┌─────────────────────────────────────────────────────────────────────┐
│                         STATELESS ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  STATELESS = Server KHÔNG lưu trạng thái của token                  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                                                               │   │
│  │   Client                         Server                       │   │
│  │     │                              │                          │   │
│  │     │  Request + JWT Token         │                          │   │
│  │     │─────────────────────────────>│                          │   │
│  │     │                              │                          │   │
│  │     │                              │  1. Verify signature     │   │
│  │     │                              │  2. Check expiration     │   │
│  │     │                              │  3. Extract claims       │   │
│  │     │                              │  ❌ KHÔNG query DB       │   │
│  │     │                              │                          │   │
│  │     │  Response                    │                          │   │
│  │     │<─────────────────────────────│                          │   │
│  │                                                               │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ƯU ĐIỂM:                                                           │
│  ✅ Không cần database cho tokens                                   │
│  ✅ Scalable - mọi server đều verify được                           │
│  ✅ Performance tốt - không I/O database                            │
│  ✅ Phù hợp microservices                                           │
│                                                                      │
│  NHƯỢC ĐIỂM:                                                        │
│  ❌ Không thể revoke token (phải đợi hết hạn)                       │
│  ❌ Logout không invalidate token                                   │
│  ❌ Không detect được token theft                                   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Kiến trúc

```
src/main/java/trinhnv/springRestfull/
│
├── config/
│   ├── SecurityConfiguration.java     ← JWT Security config
│   └── TokenConfig.java               ← Token expiration config
│
├── controller/
│   └── AuthController.java            ← /auth/* endpoints
│
├── service/
│   ├── AuthService.java               ← Login, Refresh logic
│   └── UserService.java               ← User operations
│
├── domain/
│   └── dto/
│       ├── LoginDTO.java
│       ├── ResLoginDTO.java
│       └── RefreshTokenRequestDTO.java
│
└── util/
    ├── SecurityUtil.java              ← JWT creation & verification
    ├── constant/
    │   └── TokenConstant.java
    └── error/
        ├── GlobalException.java
        └── InvalidTokenException.java
```

---

## API Endpoints

### 1. Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "trinhnv",
    "password": "123456"
}
```

**Response:**
```json
{
    "access_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmluaG52...",
    "refresh_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmluaG52...",
    "expires_in": 900,
    "refresh_expires_in": 604800,
    "token_type": "Bearer",
    "user": {
        "id": 1,
        "username": "trinhnv",
        "email": "trinh@example.com"
    }
}
```

### 2. Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response:** Giống login (tokens mới)

### 3. Logout (Client-side)
```http
POST /api/v1/auth/logout
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."  // Optional
}
```

**Response:**
```json
{
    "statusCode": 200,
    "message": "Đăng xuất thành công. Vui lòng xóa tokens ở client."
}
```

⚠️ **Note:** Với stateless, server KHÔNG invalidate token. Client PHẢI tự xóa tokens.

---

## JWT Token Structure

### Access Token
```
Header: {
    "alg": "HS512",
    "typ": "JWT"
}

Payload: {
    "sub": "trinhnv",           // Username (subject)
    "type": "access",           // Token type
    "authorities": "ROLE_USER", // User roles
    "iat": 1700000000,          // Issued at
    "exp": 1700000900           // Expires at (15 phút)
}

Signature: HMACSHA512(header.payload, secret_key)
```

### Refresh Token
```
Header: {
    "alg": "HS512",
    "typ": "JWT"
}

Payload: {
    "sub": "trinhnv",           // Username (subject)
    "type": "refresh",          // Token type
    "userId": 1,                // User ID
    "iat": 1700000000,          // Issued at
    "exp": 1700604800           // Expires at (7 ngày)
}

Signature: HMACSHA512(header.payload, secret_key)
```

---

## Flow Diagrams

### Login Flow

```
┌────────┐                      ┌────────┐                      ┌────────┐
│ Client │                      │ Server │                      │   DB   │
└───┬────┘                      └───┬────┘                      └───┬────┘
    │                               │                               │
    │  POST /auth/login             │                               │
    │  {username, password}         │                               │
    │──────────────────────────────>│                               │
    │                               │                               │
    │                               │  Validate credentials         │
    │                               │──────────────────────────────>│
    │                               │<──────────────────────────────│
    │                               │                               │
    │                               │  Create JWT Access Token      │
    │                               │  Create JWT Refresh Token     │
    │                               │  (Không lưu DB!)              │
    │                               │                               │
    │  Response                     │                               │
    │  {access_token, refresh_token}│                               │
    │<──────────────────────────────│                               │
    │                               │                               │
    │  Store tokens in client       │                               │
    │                               │                               │
```

### API Request Flow

```
┌────────┐                      ┌────────┐
│ Client │                      │ Server │
└───┬────┘                      └───┬────┘
    │                               │
    │  GET /api/users               │
    │  Authorization: Bearer <AT>   │
    │──────────────────────────────>│
    │                               │
    │                               │  1. Extract JWT from header
    │                               │  2. Verify signature (HS512)
    │                               │  3. Check expiration
    │                               │  4. Extract claims (username, roles)
    │                               │  ❌ KHÔNG query database
    │                               │
    │  Response                     │
    │  {users: [...]}               │
    │<──────────────────────────────│
    │                               │
```

### Refresh Flow

```
┌────────┐                      ┌────────┐                      ┌────────┐
│ Client │                      │ Server │                      │   DB   │
└───┬────┘                      └───┬────┘                      └───┬────┘
    │                               │                               │
    │  Access Token expired!        │                               │
    │                               │                               │
    │  POST /auth/refresh           │                               │
    │  {refreshToken: <RT>}         │                               │
    │──────────────────────────────>│                               │
    │                               │                               │
    │                               │  1. Verify JWT signature      │
    │                               │  2. Check expiration          │
    │                               │  3. Check type == "refresh"   │
    │                               │  4. Extract username          │
    │                               │                               │
    │                               │  Query user info              │
    │                               │──────────────────────────────>│
    │                               │<──────────────────────────────│
    │                               │                               │
    │                               │  Create new JWT Access Token  │
    │                               │  Create new JWT Refresh Token │
    │                               │                               │
    │  Response                     │                               │
    │  {new_access_token, new_RT}   │                               │
    │<──────────────────────────────│                               │
    │                               │                               │
```

---

## Cấu hình

### application.properties

```properties
# JWT Secret Key (Base64 encoded, min 512 bits for HS512)
trinhnguyen.jwtKey=ADhfLiqJtyTrLA9V34FNW7TTBX8WQ1HS6PM8poIugaJuKrIpf6wGPRiEViXhIQWre1v0HRhlr5IimFJSaoim4w==

# Access Token expiration (seconds) - 15 phút
trinhnguyen.access-token-expiration=900

# Refresh Token expiration (seconds) - 7 ngày
trinhnguyen.refresh-token-expiration=604800

# Max active sessions per user (không dùng trong stateless)
trinhnguyen.max-active-sessions=5
```

### TokenConfig.java

```java
@Configuration
@ConfigurationProperties(prefix = "trinhnguyen")
public class TokenConfig {
    private long accessTokenExpiration = 900;      // 15 phút
    private long refreshTokenExpiration = 604800;  // 7 ngày
    private String jwtKey;
}
```

---

## Trade-offs

### ✅ Ưu điểm của Stateless

| Feature | Benefit |
|---------|---------|
| **Không cần DB** | Không cần lưu/query tokens |
| **Scalable** | Mọi server đều verify được |
| **Performance** | Không I/O database |
| **Microservices** | Phù hợp distributed systems |
| **Simple** | Ít code, dễ maintain |

### ❌ Nhược điểm của Stateless

| Feature | Limitation |
|---------|------------|
| **Không revoke được** | Token valid đến khi hết hạn |
| **Logout** | Không invalidate được token |
| **Token theft** | Không detect/prevent được |
| **Force logout** | Không thể kick user ra |
| **Password change** | Token cũ vẫn valid |

---

## Best Practices

### Client-side

1. **Storage:**
   - Access Token: Memory (biến JS) - KHÔNG localStorage
   - Refresh Token: httpOnly cookie (tốt nhất) hoặc secure storage

2. **Auto Refresh:**
   ```javascript
   // Refresh trước khi AT hết hạn
   const refreshThreshold = 60; // 1 phút trước khi hết hạn
   
   function isTokenExpiringSoon(token) {
       const exp = decodeJWT(token).exp;
       return (exp - Date.now()/1000) < refreshThreshold;
   }
   
   async function makeRequest(url, options) {
       if (isTokenExpiringSoon(accessToken)) {
           await refreshTokens();
       }
       return fetch(url, {
           ...options,
           headers: {
               ...options.headers,
               'Authorization': `Bearer ${accessToken}`
           }
       });
   }
   ```

3. **Logout:**
   ```javascript
   function logout() {
       // Xóa tokens khỏi storage
       accessToken = null;
       refreshToken = null;
       localStorage.removeItem('refresh_token');
       
       // Redirect to login
       window.location.href = '/login';
   }
   ```

### Server-side

1. **Short Access Token:**
   - Giữ access token ngắn (15-30 phút)
   - Giảm thiểu rủi ro nếu bị đánh cắp

2. **Secure Secret Key:**
   - Dùng key đủ dài (512 bits cho HS512)
   - Lưu trong environment variables

3. **HTTPS Only:**
   - Luôn dùng HTTPS
   - Tokens truyền qua HTTP có thể bị sniff

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_TOKEN` | 401 | Token không hợp lệ hoặc hết hạn |
| `BAD_CREDENTIALS` | 400 | Username/password sai |
| `VALIDATION_ERROR` | 400 | Dữ liệu request không hợp lệ |

---

## So sánh với Stateful Approach

```
┌─────────────────────────────────────────────────────────────────────┐
│                  STATELESS vs STATEFUL                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  STATELESS (Current):                                               │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Client → JWT Token → Server verify signature → Response      │   │
│  │                       (Không query DB)                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  STATEFUL:                                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Client → Token → Server query DB → Check valid → Response    │   │
│  │                    (Query DB mỗi request)                     │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  USE STATELESS WHEN:                                                │
│  • Cần scalability cao                                              │
│  • Microservices architecture                                       │
│  • Không cần revoke tokens                                          │
│  • Performance là ưu tiên                                           │
│                                                                      │
│  USE STATEFUL WHEN:                                                 │
│  • Cần revoke tokens (logout thực sự)                               │
│  • Cần detect token theft                                           │
│  • Security là ưu tiên cao nhất                                     │
│  • Single server hoặc ít traffic                                    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Author

**trinhnv** - Spring Restful API Authentication System (Stateless JWT)
