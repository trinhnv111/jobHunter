# 🔐 GIẢI THÍCH CHI TIẾT VỀ SECURITY VÀ JWT TOKEN TRONG DỰ ÁN

## 📋 MỤC LỤC
1. [Tổng quan](#tổng-quan)
2. [Các thành phần chính](#các-thành-phần-chính)
3. [Luồng xử lý đăng nhập](#luồng-xử-lý-đăng-nhập)
4. [Sơ đồ luồng](#sơ-đồ-luồng)
5. [Phần Custom - UserDetailCustorm](#phần-custom---userdetailcustorm)
6. [Chi tiết từng bước](#chi-tiết-từng-bước)

---

## 🎯 TỔNG QUAN

Dự án này sử dụng **Spring Security** với **JWT (JSON Web Token)** để xác thực người dùng. Khi người dùng đăng nhập, hệ thống sẽ:
1. Kiểm tra username/password
2. Nếu đúng → Tạo JWT token
3. Trả về token cho client
4. Client dùng token này để truy cập các API được bảo vệ

---

## 🧩 CÁC THÀNH PHẦN CHÍNH

### 1. **SecurityConfiguration.java** 
📍 Vị trí: `config/SecurityConfiguration.java`

**Vai trò**: Cấu hình toàn bộ hệ thống Security

**Các Bean quan trọng**:
- `PasswordEncoder`: Mã hóa password bằng BCrypt
- `AuthenticationManager`: Quản lý quá trình xác thực
- `JwtEncoder`: Tạo và mã hóa JWT token
- `SecurityFilterChain`: Cấu hình các quy tắc bảo mật

```java
// Tạo PasswordEncoder để mã hóa password
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Tạo JwtEncoder để tạo token
@Bean
public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
}
```

---

### 2. **AuthController.java**
📍 Vị trí: `controller/AuthController.java`

**Vai trò**: Nhận request đăng nhập từ client

**Chức năng**:
- Nhận username/password từ client
- Gọi `AuthenticationManager` để xác thực
- Nếu thành công → Tạo token và trả về

---

### 3. **UserDetailCustorm.java** ⭐ (PHẦN CUSTOM)
📍 Vị trí: `service/UserDetailCustorm.java`

**Vai trò**: **CUSTOM** - Load thông tin user từ database

**Đây là phần bạn tự viết** để Spring Security biết cách lấy thông tin user từ database của bạn!

```java
@Override
public UserDetails loadUserByUsername(String username) {
    // Bước 1: Lấy user từ database
    User user = this.userService.hanldeUser(username);
    
    // Bước 2: Nếu không tìm thấy → throw exception
    if (user == null) {
        throw new UsernameNotFoundException("không tìm thấy: "+username);
    }
    
    // Bước 3: Trả về UserDetails cho Spring Security
    return new org.springframework.security.core.userdetails.User(
        user.getUserName(),
        user.getPassWord(),  // Password đã được mã hóa trong DB
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
}
```

---

### 4. **SecurityUtil.java**
📍 Vị trí: `util/SecurityUtil.java`

**Vai trò**: Tạo JWT token sau khi xác thực thành công

**Chức năng**:
- Nhận `Authentication` object (chứa thông tin user đã xác thực)
- Tạo JWT claims (thông tin trong token)
- Mã hóa và ký token bằng secret key
- Trả về chuỗi token

---

### 5. **UserService.java**
📍 Vị trí: `service/UserService.java`

**Vai trò**: Business logic cho User

**Chức năng**: 
- `hanldeUser(String userName)`: Tìm user theo username trong database

---

### 6. **UserRepository.java**
📍 Vị trí: `repository/UserRepository.java`

**Vai trò**: Truy cập database

**Chức năng**:
- `findByUserName(String userName)`: Query user từ database

---

## 🔄 LUỒNG XỬ LÝ ĐĂNG NHẬP

### Bước 1: Client gửi request
```
POST /login
Body: {
  "username": "trinhnv",
  "password": "123456"
}
```

### Bước 2: AuthController nhận request
```java
@PostMapping("/login")
public ResponseEntity<ResLoginDTO> login(@RequestBody LoginDTO loginDTO) {
    // Tạo token xác thực
    UsernamePasswordAuthenticationToken authenticationToken = 
        new UsernamePasswordAuthenticationToken(
            loginDTO.getUsername(), 
            loginDTO.getPassword()
        );
    
    // Gọi AuthenticationManager để xác thực
    Authentication authentication = 
        authenticationManager.authenticate(authenticationToken);
    
    // Tạo JWT token
    String accessToken = this.securityUtil.createToken(authentication);
    
    // Trả về token
    return ResponseEntity.ok().body(res);
}
```

### Bước 3: AuthenticationManager xử lý
- AuthenticationManager sẽ tự động gọi `UserDetailsService` (chính là `UserDetailCustorm` của bạn)

### Bước 4: UserDetailCustorm.loadUserByUsername() được gọi
```java
// Spring Security tự động gọi hàm này
public UserDetails loadUserByUsername(String username) {
    // Gọi UserService để lấy user từ DB
    User user = this.userService.hanldeUser(username);
    
    // Trả về UserDetails
    return new User(user.getUserName(), user.getPassWord(), ...);
}
```

### Bước 5: UserService truy vấn database
```java
public User hanldeUser(String userName) {
    return userRepository.findByUserName(userName);
}
```

### Bước 6: Spring Security so sánh password
- Lấy password từ database (đã mã hóa bằng BCrypt)
- So sánh với password người dùng nhập vào (cũng mã hóa bằng BCrypt)
- Nếu khớp → Xác thực thành công
- Nếu không khớp → Throw exception

### Bước 7: Tạo JWT token
```java
public String createToken(Authentication authentication) {
    // Tạo claims (thông tin trong token)
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuedAt(now)                    // Thời gian tạo
        .expiresAt(validity)              // Thời gian hết hạn
        .subject(authentication.getName()) // Username
        .claim("trinhnv", authentication) // Thông tin thêm
        .build();
    
    // Mã hóa và ký token
    return jwtEncoder.encode(...).getTokenValue();
}
```

### Bước 8: Trả về token cho client
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmluaG52IiwiZXhwIjoxNzM..."
}
```

---

## 📊 SƠ ĐỒ LUỒNG

```
┌─────────────┐
│   CLIENT    │
│  (Browser/  │
│   Mobile)   │
└──────┬──────┘
       │
       │ 1. POST /login
       │    {username, password}
       ▼
┌─────────────────────┐
│  AuthController     │
│  /login endpoint    │
└──────┬──────────────┘
       │
       │ 2. Tạo UsernamePasswordAuthenticationToken
       │
       ▼
┌─────────────────────┐
│ AuthenticationManager│ ◄─── Được cấu hình trong SecurityConfiguration
└──────┬──────────────┘
       │
       │ 3. Gọi UserDetailsService
       │
       ▼
┌─────────────────────┐
│ UserDetailCustorm   │ ⭐ PHẦN CUSTOM CỦA BẠN
│ loadUserByUsername()│
└──────┬──────────────┘
       │
       │ 4. Gọi UserService
       │
       ▼
┌─────────────────────┐
│   UserService       │
│   hanldeUser()      │
└──────┬──────────────┘
       │
       │ 5. Gọi UserRepository
       │
       ▼
┌─────────────────────┐
│  UserRepository     │
│  findByUserName()   │
└──────┬──────────────┘
       │
       │ 6. Query Database
       │
       ▼
┌─────────────────────┐
│     DATABASE        │
│   (MySQL)           │
└──────┬──────────────┘
       │
       │ 7. Trả về User entity
       │
       ▼
┌─────────────────────┐
│ UserDetailCustorm   │
│ Trả về UserDetails  │
└──────┬──────────────┘
       │
       │ 8. Spring Security so sánh password
       │    (BCryptPasswordEncoder)
       │
       ▼
┌─────────────────────┐
│ AuthenticationManager│
│ Trả về Authentication│
└──────┬──────────────┘
       │
       │ 9. Gọi SecurityUtil.createToken()
       │
       ▼
┌─────────────────────┐
│   SecurityUtil      │
│   createToken()     │
└──────┬──────────────┘
       │
       │ 10. Sử dụng JwtEncoder để tạo token
       │
       ▼
┌─────────────────────┐
│    JwtEncoder       │ ◄─── Được cấu hình trong SecurityConfiguration
│  (NimbusJwtEncoder) │
└──────┬──────────────┘
       │
       │ 11. Trả về JWT token string
       │
       ▼
┌─────────────────────┐
│  AuthController     │
│  Trả về ResLoginDTO │
└──────┬──────────────┘
       │
       │ 12. Response với token
       │
       ▼
┌─────────────┐
│   CLIENT    │
│ Nhận token  │
└─────────────┘
```

---

## ⭐ PHẦN CUSTOM - UserDetailCustorm

### Tại sao cần Custom?

Spring Security **KHÔNG BIẾT** cách lấy user từ database của bạn. Bạn phải **CUSTOM** bằng cách:

1. **Implement interface `UserDetailsService`**
2. **Override method `loadUserByUsername()`**
3. **Đăng ký với Spring Security**

### Cách Spring Security tìm thấy UserDetailCustorm?

```java
@Service  // ← Annotation này đăng ký với Spring
public class UserDetailCustorm implements UserDetailsService {
    // Spring sẽ tự động tìm và sử dụng class này
}
```

**NHƯNG**: Bạn cần đăng ký trong `SecurityConfiguration`:

```java
@Bean
public UserDetailsService userDetailsService() {
    return new UserDetailCustorm(userService);
}

@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

**Lưu ý**: Trong code hiện tại, bạn chưa có Bean `DaoAuthenticationProvider`, nhưng Spring Security vẫn hoạt động vì nó tự động tìm `UserDetailsService` trong context.

---

## 🔍 CHI TIẾT TỪNG BƯỚC

### Bước 1: Client gửi request đăng nhập

**Request**:
```http
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "trinhnv",
  "password": "123456"
}
```

---

### Bước 2: AuthController nhận và xử lý

```java
@PostMapping("/login")
public ResponseEntity<ResLoginDTO> login(@RequestBody LoginDTO loginDTO) {
    // Tạo token xác thực chứa username và password
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginDTO.getUsername(),  // "trinhnv"
            loginDTO.getPassword()   // "123456"
        );
    
    // Gọi AuthenticationManager để xác thực
    // → Bước này sẽ trigger toàn bộ quá trình xác thực
    Authentication authentication = 
        authenticationManager.authenticate(authenticationToken);
    
    // Nếu đến đây → Xác thực thành công
    // Tạo JWT token
    String accessToken = this.securityUtil.createToken(authentication);
    
    // Trả về token
    ResLoginDTO res = new ResLoginDTO();
    res.setToken(accessToken);
    return ResponseEntity.ok().body(res);
}
```

---

### Bước 3: AuthenticationManager xác thực

**AuthenticationManager** là trung tâm xử lý xác thực:

1. Nhận `UsernamePasswordAuthenticationToken`
2. Tìm `UserDetailsService` trong Spring context
3. Gọi `loadUserByUsername(username)` để lấy thông tin user
4. So sánh password
5. Nếu đúng → Trả về `Authentication` object
6. Nếu sai → Throw `BadCredentialsException`

---

### Bước 4: UserDetailCustorm.loadUserByUsername() ⭐

**Đây là phần CUSTOM của bạn!**

```java
@Override
public UserDetails loadUserByUsername(String username) 
    throws UsernameNotFoundException {
    
    // BƯỚC 4.1: Lấy user từ database
    // Gọi UserService → UserRepository → Database
    User user = this.userService.hanldeUser(username);
    
    // BƯỚC 4.2: Kiểm tra user có tồn tại không
    if (user == null) {
        throw new UsernameNotFoundException("không tìm thấy: "+username);
    }
    
    // BƯỚC 4.3: Tạo UserDetails object
    // UserDetails là interface của Spring Security
    // Chứa: username, password (đã mã hóa), authorities (quyền)
    return new org.springframework.security.core.userdetails.User(
        user.getUserName(),           // Username
        user.getPassWord(),           // Password (đã mã hóa BCrypt trong DB)
        Collections.singletonList(    // Authorities (quyền)
            new SimpleGrantedAuthority("ROLE_USER")
        )
    );
}
```

**Lưu ý quan trọng**:
- Password trong database **PHẢI** được mã hóa bằng BCrypt
- Nếu password chưa mã hóa → Xác thực sẽ thất bại

---

### Bước 5: UserService truy vấn database

```java
public User hanldeUser(String userName) {
    // Gọi repository để query database
    return userRepository.findByUserName(userName);
}
```

**SQL query được tạo tự động**:
```sql
SELECT * FROM user WHERE user_name = 'trinhnv'
```

---

### Bước 6: Spring Security so sánh password

**Quá trình so sánh**:

1. Lấy password từ `UserDetails` (đã mã hóa BCrypt trong DB)
   ```
   Ví dụ: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
   ```

2. Lấy password từ `UsernamePasswordAuthenticationToken` (plain text từ client)
   ```
   Ví dụ: "123456"
   ```

3. Mã hóa password từ client bằng BCrypt
   ```java
   BCryptPasswordEncoder.encode("123456")
   ```

4. So sánh 2 chuỗi đã mã hóa
   ```java
   if (encodedPasswordFromDB.equals(encodedPasswordFromClient)) {
       // Xác thực thành công
   } else {
       // Xác thực thất bại
   }
   ```

**Lưu ý**: BCrypt tự động thêm salt, nên mỗi lần mã hóa sẽ khác nhau, nhưng `matches()` vẫn so sánh được.

---

### Bước 7: Tạo JWT Token

**SecurityUtil.createToken()**:

```java
public String createToken(Authentication authentication) {
    // Thời điểm hiện tại
    Instant now = Instant.now();
    
    // Thời điểm hết hạn (từ application.properties: 86400 giây = 24 giờ)
    Instant validity = now.plus(jwtSecond, ChronoUnit.SECONDS);
    
    // Tạo claims (thông tin trong token)
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuedAt(now)                              // Thời gian tạo
        .expiresAt(validity)                        // Thời gian hết hạn
        .subject(authentication.getName())          // Username
        .claim("trinhnv", authentication)          // Thông tin thêm
        .build();
    
    // Header của JWT (thuật toán mã hóa: HS512)
    JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
    
    // Mã hóa và ký token bằng secret key
    return this.jwtEncoder.encode(
        JwtEncoderParameters.from(jwsHeader, claims)
    ).getTokenValue();
}
```

**Cấu trúc JWT Token**:
```
Header.Payload.Signature

Header: {
  "alg": "HS512",
  "typ": "JWT"
}

Payload: {
  "sub": "trinhnv",           // Username
  "iat": 1234567890,          // Thời gian tạo
  "exp": 1234654290,          // Thời gian hết hạn
  "trinhnv": {...}            // Thông tin authentication
}

Signature: HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secretKey
)
```

---

### Bước 8: Trả về token cho client

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmluaG52IiwiZXhwIjoxNzM..."
}
```

**Client sẽ dùng token này**:
```http
GET http://localhost:8080/api/users
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## 🔑 CẤU HÌNH QUAN TRỌNG

### application.properties

```properties
# Secret key để ký JWT token (PHẢI GIỮ BÍ MẬT!)
trinhnguyen.jwtKey=ADhfLiqJtyTrLA9V34FNW7TTBX8WQ1HS6PM8poIugaJuKrIpf6wGPRiEViXhIQWre1v0HRhlr5IimFJSaoim4w==

# Thời gian sống của token (giây) - 86400 = 24 giờ
trinhnguyen.jwtSecond=86400
```

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
- **UserDetailCustorm**: Bạn tự viết để Spring Security biết cách lấy user từ database của bạn

### 4. **Luồng hoạt động đơn giản**:
```
Client → AuthController → AuthenticationManager 
→ UserDetailCustorm → UserService → Database
→ So sánh password → Tạo token → Trả về client
```

### 5. **Điểm quan trọng**:
- Password trong database **PHẢI** mã hóa bằng BCrypt
- Secret key **PHẢI** giữ bí mật
- Token có thời hạn, hết hạn phải đăng nhập lại

---

## ❓ CÂU HỎI THƯỜNG GẶP

### Q1: Tại sao cần UserDetailCustorm?
**A**: Spring Security không biết cách lấy user từ database của bạn. Bạn phải tự viết.

### Q2: Password có cần mã hóa không?
**A**: CÓ! Phải mã hóa bằng BCrypt trước khi lưu vào database.

### Q3: Token hết hạn thì sao?
**A**: Client phải đăng nhập lại để lấy token mới.

### Q4: Làm sao bảo vệ các API khác?
**A**: Thêm JWT filter vào SecurityFilterChain để kiểm tra token trong mỗi request.

---

## 📝 KẾT LUẬN

Dự án này sử dụng:
- ✅ Spring Security cho xác thực
- ✅ JWT Token cho stateless authentication
- ✅ BCrypt để mã hóa password
- ✅ Custom UserDetailsService để load user từ database

**Phần Custom chính**: `UserDetailCustorm` - đây là nơi bạn tự viết logic để Spring Security biết cách lấy user từ database của bạn!

