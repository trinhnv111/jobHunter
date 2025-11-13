# 🔐 TÀI LIỆU GIẢI THÍCH CHI TIẾT VỀ SECURITY VÀ JWT TOKEN

## 📋 MỤC LỤC
1. [Tổng quan](#tổng-quan)
2. [Các thành phần chính](#các-thành-phần-chính)
3. [Luồng xử lý đăng nhập](#luồng-xử-lý-đăng-nhập)
4. [Luồng xử lý request có token](#luồng-xử-lý-request-có-token)
5. [Sơ đồ luồng chi tiết](#sơ-đồ-luồng-chi-tiết)
6. [Phần Custom](#phần-custom)
7. [Exception Handling](#exception-handling)
8. [Đánh giá chất lượng code](#đánh-giá-chất-lượng-code)

---

## 🎯 TỔNG QUAN

Dự án này sử dụng **Spring Security** với **JWT (JSON Web Token)** để xác thực người dùng theo mô hình **stateless authentication**.

### Kiến trúc tổng quan:
- **Stateless**: Không dùng session, mỗi request phải gửi kèm token
- **JWT Token**: Chứa thông tin user và thời gian hết hạn
- **BCrypt**: Mã hóa password một chiều
- **Custom UserDetailsService**: Load user từ database

---

## 🧩 CÁC THÀNH PHẦN CHÍNH

### 1. **SecurityConfiguration.java** 
📍 `config/SecurityConfiguration.java`

**Vai trò**: Cấu hình toàn bộ hệ thống Security

**Các Bean quan trọng**:
- `PasswordEncoder`: BCryptPasswordEncoder để mã hóa password
- `AuthenticationManager`: Quản lý quá trình xác thực
- `JwtEncoder`: Tạo và mã hóa JWT token
- `JwtDecoder`: Giải mã và xác thực JWT token
- `SecurityFilterChain`: Cấu hình quy tắc bảo mật

---

### 2. **AuthController.java**
📍 `controller/AuthController.java`

**Vai trò**: Xử lý endpoint đăng nhập `/login`

**Chức năng**:
- Nhận username/password từ client
- Gọi `AuthenticationManager` để xác thực
- Tạo JWT token nếu thành công
- Trả về token cho client

---

### 3. **UserDetailCustorm.java** ⭐ (PHẦN CUSTOM)
📍 `service/UserDetailCustorm.java`

**Vai trò**: **CUSTOM** - Load user từ database

**Đây là phần bạn tự viết** để Spring Security biết cách lấy user từ database!

**Cách hoạt động**:
- Implement `UserDetailsService`
- Override `loadUserByUsername()`
- Spring Security tự động tìm và sử dụng class này

---

### 4. **SecurityUtil.java**
📍 `util/SecurityUtil.java`

**Vai trò**: Tạo JWT token sau khi xác thực thành công

**Chức năng**:
- Nhận `Authentication` object
- Tạo JWT claims (thông tin trong token)
- Mã hóa và ký token bằng secret key
- Trả về chuỗi token

---

### 5. **CustomAuthenticationEntryPoint.java** ⭐ (PHẦN CUSTOM)
📍 `config/CustomAuthenticationEntryPoint.java`

**Vai trò**: **CUSTOM** - Xử lý lỗi 401 khi token không hợp lệ

**Chức năng**:
- Xử lý khi token không tồn tại
- Xử lý khi token không hợp lệ
- Xử lý khi token hết hạn
- Trả về response JSON với message lỗi

---

## 🔄 LUỒNG XỬ LÝ ĐĂNG NHẬP

### Bước 1: Client gửi request đăng nhập
```
POST /login
Content-Type: application/json

{
  "username": "trinhnv",
  "password": "123456"
}
```

### Bước 2: AuthController nhận request
- Validate input (`@Valid`)
- Tạo `UsernamePasswordAuthenticationToken`

### Bước 3: AuthenticationManager xử lý
- Tìm `UserDetailsService` (UserDetailCustorm) trong Spring context
- Gọi `loadUserByUsername(username)`

### Bước 4: UserDetailCustorm.loadUserByUsername() ⭐
- Gọi `UserService.hanldeUser(username)`
- UserService → UserRepository → Database
- Query: `SELECT * FROM user WHERE user_name = ?`
- Trả về `UserDetails` object

### Bước 5: Spring Security so sánh password
- Lấy password từ `UserDetails` (đã mã hóa BCrypt trong DB)
- Lấy password từ request (plain text)
- Mã hóa password từ request bằng BCrypt
- So sánh 2 chuỗi đã mã hóa
- Nếu khớp → Xác thực thành công
- Nếu không khớp → Throw `BadCredentialsException`

### Bước 6: Tạo JWT token
- Gọi `SecurityUtil.createToken(authentication)`
- Tạo JWT claims (username, thời gian hết hạn...)
- Mã hóa và ký token bằng secret key
- Trả về token string

### Bước 7: Trả về token cho client
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## 🔒 LUỒNG XỬ LÝ REQUEST CÓ TOKEN

### Bước 1: Client gửi request với token
```
GET /users
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Bước 2: Spring Security Filter Chain
- Kiểm tra `authorizeHttpRequests`
- Nếu `permitAll` → Cho phép truy cập
- Nếu `authenticated` → Kiểm tra token

### Bước 3: JwtDecoder giải mã token
- Lấy token từ header `Authorization: Bearer <token>`
- Gọi `JwtDecoder.decode(token)`
- Kiểm tra signature bằng secret key
- Kiểm tra thời gian hết hạn
- Nếu hợp lệ → Tạo `Authentication` object
- Nếu không hợp lệ → Throw exception

### Bước 4: Xử lý exception (nếu có)
- Nếu token không hợp lệ → Gọi `CustomAuthenticationEntryPoint.commence()`
- Trả về response 401 với message lỗi

### Bước 5: Cho phép truy cập endpoint
- Nếu token hợp lệ → Cho phép truy cập
- Controller xử lý request và trả về response

---

## 📊 SƠ ĐỒ LUỒNG CHI TIẾT

### Sơ đồ 1: Luồng đăng nhập và tạo token

```
┌─────────────────────────────────────────────────────────────────┐
│                    BƯỚC 1: CLIENT GỬI REQUEST                    │
└─────────────────────────────────────────────────────────────────┘

    [CLIENT]
       │
       │ POST /login
       │ {
       │   "username": "trinhnv",
       │   "password": "123456"
       │ }
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│              BƯỚC 2: AuthController NHẬN REQUEST                │
│                    (controller/AuthController.java)              │
└─────────────────────────────────────────────────────────────────┘

    [AuthController.login()]
       │
       │ 1. Validate input (@Valid)
       │ 2. Tạo UsernamePasswordAuthenticationToken
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│        BƯỚC 3: AuthenticationManager XỬ LÝ XÁC THỰC              │
│              (config/SecurityConfiguration.java)                 │
│                                                                  │
│  AuthenticationManager tự động:                                  │
│  1. Tìm UserDetailsService trong Spring context                 │
│  2. Tìm thấy UserDetailCustorm                                  │
│  3. Gọi loadUserByUsername(username)                            │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Gọi UserDetailCustorm.loadUserByUsername()
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│     BƯỚC 4: UserDetailCustorm.loadUserByUsername() ⭐ CUSTOM    │
│              (service/UserDetailCustorm.java)                    │
│                                                                  │
│  ═══════════════════════════════════════════════════════════     │
│  ĐÂY LÀ PHẦN BẠN TỰ VIẾT!                                        │
│  ═══════════════════════════════════════════════════════════     │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Gọi UserService.hanldeUser(username)
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│          BƯỚC 5: UserService TRUY VẤN DATABASE                 │
│                   (service/UserService.java)                     │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Gọi UserRepository.findByUserName(username)
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│        BƯỚC 6: UserRepository QUERY DATABASE                     │
│              (repository/UserRepository.java)                      │
└─────────────────────────────────────────────────────────────────┘

       │
       │ SELECT * FROM user WHERE user_name = 'trinhnv'
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                  BƯỚC 7: DATABASE TRẢ VỀ                         │
│                        (MySQL Database)                         │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Trả về User entity:
       │ {
       │   id: 1,
       │   userName: "trinhnv",
       │   passWord: "$2a$10$..." (đã mã hóa BCrypt)
       │ }
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│   BƯỚC 8: UserDetailCustorm TẠO UserDetails OBJECT             │
│              (service/UserDetailCustorm.java)                     │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Trả về UserDetails
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│    BƯỚC 9: AuthenticationManager SO SÁNH PASSWORD                │
│              (config/SecurityConfiguration.java)                  │
│                                                                  │
│  Spring Security tự động:                                       │
│  1. Lấy password từ UserDetails (đã mã hóa BCrypt)               │
│  2. Lấy password từ request (plain text: "123456")              │
│  3. Mã hóa password từ request bằng BCryptPasswordEncoder         │
│  4. So sánh 2 chuỗi đã mã hóa                                   │
│                                                                  │
│  Nếu KHỚP → Xác thực thành công                                  │
│  Nếu KHÔNG KHỚP → Throw BadCredentialsException                  │
│     → Xử lý bởi GlobalException.handleUserPrincipalNotFound()   │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Password khớp → Tạo Authentication object
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│     BƯỚC 10: AuthController GỌI SecurityUtil.createToken()       │
│                  (controller/AuthController.java)                │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Gọi securityUtil.createToken(authentication)
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│            BƯỚC 11: SecurityUtil TẠO JWT TOKEN                 │
│                     (util/SecurityUtil.java)                     │
│                                                                  │
│  1. Tạo JwtClaimsSet:                                           │
│     - subject: username ("trinhnv")                              │
│     - issuedAt: thời gian tạo                                    │
│     - expiresAt: thời gian hết hạn (24 giờ sau)                  │
│     - claim: thông tin authentication                            │
│                                                                  │
│  2. Tạo JwsHeader:                                              │
│     - algorithm: HS512                                          │
│                                                                  │
│  3. Mã hóa và ký token bằng JwtEncoder:                         │
│     - Sử dụng secret key từ application.properties               │
│     - Tạo chuỗi token: "eyJhbGciOiJIUzUxMiJ9..."                 │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Trả về token string
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│         BƯỚC 12: AuthController TRẢ VỀ TOKEN                   │
│                  (controller/AuthController.java)                │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Response:
       │ {
       │   "token": "eyJhbGciOiJIUzUxMiJ9..."
       │ }
       │
       ▼
    [CLIENT]
    Nhận token và lưu lại
    Dùng token này cho các request tiếp theo
```

### Sơ đồ 2: Luồng xử lý request có token

```
┌─────────────────────────────────────────────────────────────────┐
│              BƯỚC 1: CLIENT GỬI REQUEST VỚI TOKEN                │
└─────────────────────────────────────────────────────────────────┘

    [CLIENT]
       │
       │ GET /users
       │ Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│        BƯỚC 2: SPRING SECURITY FILTER CHAIN                     │
│              (config/SecurityConfiguration.java)                 │
│                                                                  │
│  SecurityFilterChain kiểm tra:                                  │
│  1. authorizeHttpRequests                                        │
│     - requestMatchers("/","/login").permitAll()                 │
│     - anyRequest().authenticated()                              │
│  2. Nếu authenticated → Kiểm tra token                         │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Request cần authenticated → Kiểm tra token
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│          BƯỚC 3: JWT DECODER GIẢI MÃ TOKEN                      │
│              (config/SecurityConfiguration.java)                 │
│                                                                  │
│  JwtDecoder.decode(token):                                       │
│  1. Lấy token từ header Authorization: Bearer <token>           │
│  2. Kiểm tra signature bằng secret key                          │
│  3. Kiểm tra thời gian hết hạn                                   │
│  4. Nếu hợp lệ → Tạo Authentication object                      │
│  5. Nếu không hợp lệ → Throw exception                          │
└─────────────────────────────────────────────────────────────────┘

       │
       │ Token hợp lệ → Cho phép truy cập
       │ Token không hợp lệ → Throw exception
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│     BƯỚC 4A: TOKEN HỢP LỆ → CHO PHÉP TRUY CẬP                    │
│                                                                  │
│  Controller xử lý request và trả về response                    │
└─────────────────────────────────────────────────────────────────┘

       │
       │ HOẶC
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│   BƯỚC 4B: TOKEN KHÔNG HỢP LỆ → XỬ LÝ EXCEPTION ⭐ CUSTOM       │
│        (config/CustomAuthenticationEntryPoint.java)               │
│                                                                  │
│  CustomAuthenticationEntryPoint.commence():                      │
│  1. Set HTTP status code 401                                    │
│  2. Set WWW-Authenticate header                                  │
│  3. Tạo ApiResponse với thông tin lỗi                           │
│  4. Convert thành JSON và trả về client                          │
│                                                                  │
│  Response:                                                      │
│  {                                                               │
│    "statusCode": 401,                                            │
│    "error": "JWT expired at...",                                │
│    "message": "Token hết hạn,không hợp lệ,........."            │
│  }                                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## ⭐ PHẦN CUSTOM

### 1. UserDetailCustorm ⭐

**Vị trí**: `service/UserDetailCustorm.java`

**Tại sao cần Custom?**
- Spring Security **KHÔNG BIẾT** cách lấy user từ database của bạn
- Bạn phải tự implement `UserDetailsService`

**Cách Spring Security tìm thấy?**
- `@Service` annotation đăng ký với Spring context
- Spring Security tự động tìm `UserDetailsService` trong context
- Khi `AuthenticationManager.authenticate()` được gọi → Tự động gọi `loadUserByUsername()`

**Chèn vào bước nào?**
- **Bước 4** trong luồng đăng nhập
- Khi `AuthenticationManager` cần load user từ database

**Code mẫu**:
```java
@Service
public class UserDetailCustorm implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. Lấy user từ database
        User user = userService.hanldeUser(username);
        
        // 2. Kiểm tra user có tồn tại không
        if (user == null) {
            throw new UsernameNotFoundException("không tìm thấy: " + username);
        }
        
        // 3. Trả về UserDetails cho Spring Security
        return new User(
            user.getUserName(),
            user.getPassWord(),  // PHẢI mã hóa BCrypt!
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
```

---

### 2. CustomAuthenticationEntryPoint ⭐

**Vị trí**: `config/CustomAuthenticationEntryPoint.java`

**Tại sao cần Custom?**
- Default handler của Spring Security trả về response đơn giản
- Bạn muốn trả về response JSON với message lỗi chi tiết
- Hỗ trợ tiếng Việt trong response

**Cách đăng ký?**
- Trong `SecurityConfiguration.securityFilterChain()`:
```java
.oauth2ResourceServer((oauth2) -> oauth2
    .authenticationEntryPoint(customAuthenticationEntryPoint)
)
```

**Chèn vào bước nào?**
- **Bước 4B** trong luồng xử lý request có token
- Khi token không hợp lệ → Spring Security gọi `commence()`

**Code mẫu**:
```java
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(...) {
        // 1. Set HTTP status code 401
        // 2. Tạo ApiResponse với thông tin lỗi
        // 3. Convert thành JSON và trả về client
    }
}
```

---

## 🚨 EXCEPTION HANDLING

### 1. Exception trong quá trình đăng nhập

#### BadCredentialsException
**Khi nào xảy ra?**
- Username/password sai
- Password không khớp với database

**Xử lý**:
- `GlobalException.handleUserPrincipalNotFound()`
- Trả về 400 Bad Request với message "Bad Credentials"

#### UsernameNotFoundException
**Khi nào xảy ra?**
- Username không tồn tại trong database

**Xử lý**:
- `UserDetailCustorm.loadUserByUsername()` throw exception
- Spring Security xử lý → Trả về 401 Unauthorized

#### MethodArgumentNotValidException
**Khi nào xảy ra?**
- Validation fail (username/password rỗng)

**Xử lý**:
- `GlobalException.handleMethodArgumentNotValidException()`
- Trả về 400 Bad Request với danh sách lỗi validation

---

### 2. Exception trong quá trình xác thực token

#### MissingBearerTokenException
**Khi nào xảy ra?**
- Client không gửi header `Authorization`
- Header không có format `Bearer <token>`

**Xử lý**:
- `CustomAuthenticationEntryPoint.commence()`
- Trả về 401 Unauthorized với message lỗi

#### JwtException
**Khi nào xảy ra?**
- Token không hợp lệ (signature sai, format sai)

**Xử lý**:
- `JwtDecoder.decode()` throw exception
- `CustomAuthenticationEntryPoint.commence()`
- Trả về 401 Unauthorized với message lỗi

#### ExpiredJwtException
**Khi nào xảy ra?**
- Token đã hết hạn (quá thời gian `expiresAt`)

**Xử lý**:
- `JwtDecoder.decode()` throw exception
- `CustomAuthenticationEntryPoint.commence()`
- Trả về 401 Unauthorized với message "Token hết hạn"

---

## 📈 ĐÁNH GIÁ CHẤT LƯỢNG CODE

### ✅ Điểm mạnh:

1. **Cấu trúc rõ ràng**
   - Tách biệt các layer: Controller, Service, Repository
   - Code dễ đọc và maintain

2. **Security tốt**
   - Sử dụng BCrypt để mã hóa password
   - JWT token có thời gian hết hạn
   - Secret key được cấu hình từ application.properties

3. **Exception handling đầy đủ**
   - Custom exception handler cho các trường hợp lỗi
   - Response JSON với message lỗi rõ ràng

4. **Custom implementation đúng cách**
   - UserDetailCustorm implement UserDetailsService đúng chuẩn
   - CustomAuthenticationEntryPoint xử lý lỗi tốt

### ⚠️ Điểm cần cải thiện:

1. **Logging**
   - Nên dùng logger thay vì `System.out.println()`
   - Log các exception để debug dễ hơn

2. **Error handling trong CustomAuthenticationEntryPoint**
   - `authException.getCause()` có thể null → Đã xử lý ✅
   - Có thể customize message theo từng loại exception

3. **Secret key**
   - Nên lưu trong environment variable hoặc secret management
   - Không nên commit vào Git

4. **Token refresh**
   - Chưa có cơ chế refresh token
   - User phải đăng nhập lại khi token hết hạn

5. **Role-based access control**
   - Hiện tại tất cả user đều có `ROLE_USER`
   - Có thể mở rộng để phân quyền chi tiết hơn

---

## 🎓 TÓM TẮT CHO NGƯỜI MỚI

### 1. **Spring Security làm gì?**
- Quản lý xác thực (authentication) và phân quyền (authorization)
- Bảo vệ các API endpoint

### 2. **JWT Token là gì?**
- Một chuỗi mã hóa chứa thông tin user
- Client gửi token trong header để chứng minh đã đăng nhập
- Token có thời hạn (24 giờ trong dự án này)

### 3. **Phần Custom ở đâu?**
- **UserDetailCustorm**: Load user từ database
- **CustomAuthenticationEntryPoint**: Xử lý lỗi 401

### 4. **Luồng hoạt động đơn giản**:
```
Đăng nhập:
Client → AuthController → AuthenticationManager 
→ UserDetailCustorm → UserService → Database
→ So sánh password → Tạo token → Trả về client

Request có token:
Client → Spring Security → JwtDecoder → Kiểm tra token
→ Nếu hợp lệ → Cho phép truy cập
→ Nếu không hợp lệ → CustomAuthenticationEntryPoint → 401
```

### 5. **Điểm quan trọng**:
- Password trong database **PHẢI** mã hóa bằng BCrypt
- Secret key **PHẢI** giữ bí mật
- Token có thời hạn, hết hạn phải đăng nhập lại
- Custom exception handler để trả về response JSON đẹp

---

## 📝 KẾT LUẬN

Dự án này sử dụng:
- ✅ Spring Security cho xác thực
- ✅ JWT Token cho stateless authentication
- ✅ BCrypt để mã hóa password
- ✅ Custom UserDetailsService để load user từ database
- ✅ Custom AuthenticationEntryPoint để xử lý lỗi 401

**Phần Custom chính**:
1. `UserDetailCustorm` - Load user từ database
2. `CustomAuthenticationEntryPoint` - Xử lý lỗi khi token không hợp lệ

Code đã được comment chi tiết và dễ hiểu cho người mới!

