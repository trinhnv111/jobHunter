# 📋 HƯỚNG DẪN SỬ DỤNG FILTER TRONG DỰ ÁN

## 📌 Tổng quan

Dự án sử dụng thư viện **SpringFilter** để thực hiện lọc động dữ liệu thông qua query parameters. Filter cho phép client gửi các điều kiện lọc trực tiếp trong URL mà không cần viết code Specification thủ công.

## 🔧 Cài đặt

### Dependency

Thư viện đã được cấu hình trong `build.gradle.kts`:

```kotlin
implementation("com.turkraft.springfilter:jpa:3.2.4")
```

### Cấu hình Repository

Repository cần extends `JpaSpecificationExecutor` để hỗ trợ Specification:

```java
public interface CompanyRespository extends JpaRepository<Company, Long>,
        JpaSpecificationExecutor<Company> {
}
```

## 🚀 Cách sử dụng

### Endpoint hỗ trợ Filter

**GET** `/api/v1/company`

### Cấu trúc Request

```
GET /api/v1/company?filter={điều_kiện_lọc}&page={số_trang}&size={kích_thước}&sort={sắp_xếp}
```

### Tham số

- **filter** (optional): Điều kiện lọc dữ liệu
- **page** (optional): Số trang (bắt đầu từ 0), mặc định: 0
- **size** (optional): Số lượng bản ghi mỗi trang, mặc định: 10
- **sort** (optional): Sắp xếp (ví dụ: `name,asc` hoặc `id,desc`)

## 📚 Các toán tử Filter được hỗ trợ

### 1. Toán tử so sánh

| Toán tử | Cú pháp | Ví dụ | Mô tả |
|---------|---------|-------|-------|
| Bằng | `field:'value'` | `name:'ABC'` | Tìm chính xác |
| Khác | `field!:'value'` | `name!:'ABC'` | Khác với giá trị |
| Lớn hơn | `field:>value` | `id:>10` | Lớn hơn |
| Nhỏ hơn | `field:<value` | `id:<100` | Nhỏ hơn |
| Lớn hơn hoặc bằng | `field:>=value` | `id:>=10` | Lớn hơn hoặc bằng |
| Nhỏ hơn hoặc bằng | `field:<=value` | `id:<=100` | Nhỏ hơn hoặc bằng |

### 2. Toán tử chuỗi

| Toán tử | Cú pháp | Ví dụ | Mô tả |
|---------|---------|-------|-------|
| Contains | `field:contains:'text'` | `name:contains:'Tech'` | Chứa chuỗi |
| StartsWith | `field:startsWith:'text'` | `name:startsWith:'A'` | Bắt đầu bằng |
| EndsWith | `field:endsWith:'text'` | `name:endsWith:'Inc'` | Kết thúc bằng |

### 3. Toán tử NULL

| Toán tử | Cú pháp | Ví dụ | Mô tả |
|---------|---------|-------|-------|
| IsNull | `field:isNull` | `address:isNull` | Giá trị NULL |
| IsNotNull | `field:isNotNull` | `address:isNotNull` | Giá trị không NULL |

### 4. Toán tử IN

| Toán tử | Cú pháp | Ví dụ | Mô tả |
|---------|---------|-------|-------|
| In | `field:in:(value1,value2)` | `id:in:(1,2,3)` | Trong danh sách |

### 5. Toán tử logic

| Toán tử | Cú pháp | Ví dụ | Mô tả |
|---------|---------|-------|-------|
| AND | `condition1 and condition2` | `name:'ABC' and id:>10` | Và |
| OR | `condition1 or condition2` | `name:'ABC' or name:'XYZ'` | Hoặc |
| NOT | `not condition` | `not name:'ABC'` | Phủ định |

## 💡 Ví dụ sử dụng

### Ví dụ 1: Lọc đơn giản

**Request:**
```
GET /api/v1/company?filter=name:'ABC Company'
```

**Mô tả:** Tìm công ty có tên chính xác là "ABC Company"

**SQL tương đương:**
```sql
SELECT * FROM companies WHERE name = 'ABC Company'
```

### Ví dụ 2: Lọc với Contains

**Request:**
```
GET /api/v1/company?filter=name:contains:'Tech'
```

**Mô tả:** Tìm công ty có tên chứa từ "Tech"

**SQL tương đương:**
```sql
SELECT * FROM companies WHERE name LIKE '%Tech%'
```

### Ví dụ 3: Lọc với điều kiện AND

**Request:**
```
GET /api/v1/company?filter=name:contains:'Tech' and address:isNotNull
```

**Mô tả:** Tìm công ty có tên chứa "Tech" VÀ có địa chỉ

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE name LIKE '%Tech%' AND address IS NOT NULL
```

### Ví dụ 4: Lọc với điều kiện OR

**Request:**
```
GET /api/v1/company?filter=name:'ABC' or name:'XYZ'
```

**Mô tả:** Tìm công ty có tên là "ABC" HOẶC "XYZ"

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE name = 'ABC' OR name = 'XYZ'
```

### Ví dụ 5: Lọc với so sánh số

**Request:**
```
GET /api/v1/company?filter=id:>10 and id:<100
```

**Mô tả:** Tìm công ty có ID lớn hơn 10 và nhỏ hơn 100

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE id > 10 AND id < 100
```

### Ví dụ 6: Lọc với IN

**Request:**
```
GET /api/v1/company?filter=id:in:(1,2,3,5,8)
```

**Mô tả:** Tìm công ty có ID trong danh sách [1, 2, 3, 5, 8]

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE id IN (1, 2, 3, 5, 8)
```

### Ví dụ 7: Kết hợp với Pagination

**Request:**
```
GET /api/v1/company?filter=name:contains:'Tech'&page=0&size=20
```

**Mô tả:** Tìm công ty có tên chứa "Tech", trang đầu tiên, mỗi trang 20 bản ghi

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE name LIKE '%Tech%' 
LIMIT 20 OFFSET 0
```

### Ví dụ 8: Kết hợp với Sorting

**Request:**
```
GET /api/v1/company?filter=name:contains:'Tech'&page=0&size=10&sort=name,asc
```

**Mô tả:** Tìm công ty có tên chứa "Tech", sắp xếp theo tên tăng dần

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE name LIKE '%Tech%' 
ORDER BY name ASC
LIMIT 10 OFFSET 0
```

### Ví dụ 9: Lọc phức tạp với nhiều điều kiện

**Request:**
```
GET /api/v1/company?filter=(name:contains:'Tech' or name:contains:'IT') and address:isNotNull and id:>5
```

**Mô tả:** Tìm công ty có:
- Tên chứa "Tech" HOẶC "IT"
- VÀ có địa chỉ
- VÀ ID lớn hơn 5

**SQL tương đương:**
```sql
SELECT * FROM companies 
WHERE (name LIKE '%Tech%' OR name LIKE '%IT%') 
AND address IS NOT NULL 
AND id > 5
```

### Ví dụ 10: Không có Filter (Lấy tất cả)

**Request:**
```
GET /api/v1/company?page=0&size=10
```

**Mô tả:** Lấy tất cả công ty, phân trang

**SQL tương đương:**
```sql
SELECT * FROM companies 
LIMIT 10 OFFSET 0
```

## 📊 Cấu trúc Response

### Response thành công

```json
{
  "meta": {
    "page": 1,
    "pageSize": 10,
    "total": 25,
    "pages": 3
  },
  "result": [
    {
      "id": 1,
      "name": "ABC Company",
      "description": "Mô tả công ty",
      "address": "123 Đường ABC",
      "logo": "logo.png",
      "createdDate": "2024-01-01 10:00:00 AM",
      "updatedAt": "2024-01-02 11:00:00 AM"
    },
    {
      "id": 2,
      "name": "XYZ Corporation",
      "description": "Mô tả công ty",
      "address": "456 Đường XYZ",
      "logo": "logo2.png",
      "createdDate": "2024-01-03 09:00:00 AM",
      "updatedAt": "2024-01-04 10:00:00 AM"
    }
  ]
}
```

### Giải thích Response

- **meta**: Thông tin phân trang
  - `page`: Trang hiện tại (bắt đầu từ 1)
  - `pageSize`: Số lượng bản ghi mỗi trang
  - `total`: Tổng số bản ghi
  - `pages`: Tổng số trang
- **result**: Mảng các CompanyDTO đã được filter

## 📖 Lý thuyết về Page, Pageable và Specification

### 1. Pageable là gì?

**Pageable** là một interface trong Spring Data để đại diện cho thông tin phân trang và sắp xếp.

#### Các thành phần của Pageable:

- **Page Number**: Số trang (bắt đầu từ 0)
- **Page Size**: Số lượng bản ghi mỗi trang
- **Sort**: Thông tin sắp xếp (field và direction)

#### Cách Spring tự động tạo Pageable:

Khi client gửi request với query parameters:
```
GET /api/v1/company?page=0&size=10&sort=name,asc
```

Spring tự động parse và tạo `Pageable` object:
```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
```

#### Các method quan trọng của Pageable:

```java
int getPageNumber()      // Trả về số trang (0-based)
int getPageSize()        // Trả về kích thước trang
Sort getSort()           // Trả về thông tin sắp xếp
long getOffset()         // Trả về offset (pageNumber * pageSize)
```

### 2. Page là gì?

**Page** là một interface extends `Slice` trong Spring Data, đại diện cho một trang dữ liệu đã được phân trang.

#### Các thành phần của Page:

- **Content**: Danh sách các entity trong trang hiện tại (`List<T>`)
- **Total Elements**: Tổng số bản ghi thỏa mãn điều kiện
- **Total Pages**: Tổng số trang
- **Number**: Số trang hiện tại (0-based)
- **Size**: Kích thước trang
- **First/Last**: Trang đầu/cuối
- **HasNext/HasPrevious**: Có trang tiếp theo/trước

#### Ví dụ Page object:

```java
Page<Company> page = repository.findAll(pageable);

page.getContent()           // List<Company> - Dữ liệu trang hiện tại
page.getTotalElements()     // long - Tổng số bản ghi (ví dụ: 25)
page.getTotalPages()       // int - Tổng số trang (ví dụ: 3)
page.getNumber()           // int - Trang hiện tại (0-based)
page.getSize()             // int - Kích thước trang
page.isFirst()             // boolean - Có phải trang đầu?
page.isLast()              // boolean - Có phải trang cuối?
page.hasNext()             // boolean - Có trang tiếp theo?
page.hasPrevious()         // boolean - Có trang trước?
```

### 2.1. Giải Thích Chi Tiết và Cụ Thể Hơn - Ví Dụ Cửa Hàng

Hãy để tôi giải thích một cách đơn giản hơn thông qua một ví dụ thực tế:

#### Tưởng Tượng Bạn Có Một Cửa Hàng

Cửa hàng của bạn có **100 sản phẩm**. Khách hàng vào cửa hàng và nói: *"Tôi muốn xem trang đầu tiên, mỗi trang 10 sản phẩm"*

- **Pageable** = **Yêu cầu của khách hàng**: "Trang 0, 10 sản phẩm"
- **Page** = **Phiếu trả lời của cửa hàng**: "Đây là 10 sản phẩm trang đầu, có tổng cộng 100 sản phẩm, 10 trang"

#### Sơ Đồ Luồng Xử Lý Pageable và Page

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CLIENT (Người dùng)                              │
│                                                                     │
│  Gửi Request:                                                       │
│  GET /api/v1/company?page=0&size=5                                 │
│                                                                     │
│  Ý nghĩa: "Cho tôi xem trang 0 (trang đầu), mỗi trang 5 sản phẩm" │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              CONTROLLER LAYER                                        │
│                                                                     │
│  @GetMapping("/company")                                           │
│  public Page<Company> getAllCompany(Pageable pageable) {           │
│      // Spring tự động parse query params → Pageable               │
│      // Pageable = PageRequest.of(0, 5)                           │
│      //         ↑              ↑                                   │
│      //    trang 0        5 sản phẩm                                │
│                                                                     │
│      return companyService.findAll(pageable);                      │
│  }                                                                  │
│                                                                     │
│  ⭐ Pageable ở đây = "CHỈ DẪN" để database biết cần lấy gì        │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              SERVICE LAYER                                          │
│                                                                     │
│  public Page<Company> findAll(Pageable pageable) {                 │
│      // Truyền Pageable xuống Repository                          │
│      // Repository hiểu: "lấy trang 0, mỗi trang 5 sản phẩm"      │
│      return repository.findAll(pageable);                          │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              REPOSITORY LAYER                                       │
│                                                                     │
│  public interface CompanyRepository                                 │
│      extends JpaRepository<Company, Long> {                        │
│  }                                                                  │
│                                                                     │
│  // Spring Data JPA tự động implement method:                      │
│  Page<Company> findAll(Pageable pageable);                         │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              DATABASE (MySQL)                                       │
│                                                                     │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  TRUY VẤN 1: Lấy dữ liệu (5 sản phẩm trang đầu)              ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│                                                                     │
│  SELECT * FROM companies                                           │
│  LIMIT 5 OFFSET 0;                                                │
│                                                                     │
│  Kết quả: 5 Company entities                                      │
│  ┌─────────┬──────────────┐                                      │
│  │ id      │ name         │                                      │
│  ├─────────┼──────────────┤                                      │
│  │ 1       │ Company A    │                                      │
│  │ 2       │ Company B    │                                      │
│  │ 3       │ Company C    │                                      │
│  │ 4       │ Company D    │                                      │
│  │ 5       │ Company E    │                                      │
│  └─────────┴──────────────┘                                      │
│                                                                     │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  TRUY VẤN 2: Đếm tổng số sản phẩm                             ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│                                                                     │
│  SELECT COUNT(*) FROM companies;                                  │
│                                                                     │
│  Kết quả: 100 (tổng số companies trong database)                  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              SPRING DATA JPA TẠO PAGE OBJECT                        │
│                                                                     │
│  Page<Company> page = new PageImpl<>(                             │
│      content,        // List<Company> - 5 entities                 │
│      pageable,       // PageRequest(page=0, size=5)                │
│      total           // long - 100 (tổng số)                      │
│  );                                                                 │
│                                                                     │
│  ⭐ Page object chứa:                                              │
│  {                                                                  │
│    "content": [                                                    │
│      { "id": 1, "name": "Company A" },                            │
│      { "id": 2, "name": "Company B" },                            │
│      { "id": 3, "name": "Company C" },                            │
│      { "id": 4, "name": "Company D" },                            │
│      { "id": 5, "name": "Company E" }                             │
│    ],                                                              │
│    "totalElements": 100,  // ← Tổng số companies                  │
│    "totalPages": 20,       // ← Tổng số trang (100/5=20)         │
│    "number": 0,            // ← Trang hiện tại                    │
│    "size": 5,               // ← Số companies mỗi trang          │
│    "hasNext": true,         // ← Có trang tiếp không?             │
│    "hasPrevious": false     // ← Có trang trước không?             │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│              RESPONSE TRẢ VỀ CLIENT                                 │
│                                                                     │
│  HTTP 200 OK                                                        │
│  {                                                                  │
│    "meta": {                                                        │
│      "page": 1,        // Trang hiện tại (1-based)                 │
│      "pageSize": 5,    // Số phần tử mỗi trang                     │
│      "total": 100,     // Tổng số phần tử                           │
│      "pages": 20       // Tổng số trang                             │
│    },                                                               │
│    "result": [                                                     │
│      { "id": 1, "name": "Company A" },                            │
│      { "id": 2, "name": "Company B" },                            │
│      { "id": 3, "name": "Company C" },                            │
│      { "id": 4, "name": "Company D" },                            │
│      { "id": 5, "name": "Company E" }                             │
│    ]                                                                │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

#### Ví Dụ Code Cụ Thể

##### Bước 1: Client (Người dùng) Gửi Request

```
GET /api/v1/company?page=0&size=5
```

Đây là client nói: "Cho tôi xem trang 0 (trang đầu), mỗi trang 5 công ty"

##### Bước 2: Backend Nhận Request và Tạo Pageable

```java
@GetMapping("/company")
public Page<Company> getCompanies(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size) {
    
    // Tạo Pageable - đây là các "chỉ dẫn" để database biết cái gì cần lấy
    Pageable pageable = PageRequest.of(page, size);
    //                                 ^         ^
    //                           trang 0    5 công ty
    
    // Gửi Pageable xuống database
    return companyService.findAll(pageable);
}
```

##### Bước 3: Service Gửi Pageable Xuống Repository

```java
@Service
public class CompanyService {
    
    @Autowired
    private CompanyRepository repository;
    
    public Page<Company> findAll(Pageable pageable) {
        // Truyền Pageable vào repository
        // Repository hiểu: "lấy trang 0, mỗi trang 5 công ty từ database"
        return repository.findAll(pageable);
    }
}
```

##### Bước 4: Database Chạy 2 Truy Vấn SQL

```sql
-- Truy vấn 1: Lấy dữ liệu (trang 0, 5 công ty)
SELECT * FROM companies LIMIT 5 OFFSET 0;

-- Truy vấn 2: Đếm tổng số công ty
SELECT COUNT(*) FROM companies;
```

##### Bước 5: Kết Quả Được Trả Về (Page Object)

```java
// Spring tự động tạo Page object chứa:
{
  "content": [          // ← Dữ liệu thực tế (5 công ty)
    { "id": 1, "name": "Company A" },
    { "id": 2, "name": "Company B" },
    { "id": 3, "name": "Company C" },
    { "id": 4, "name": "Company D" },
    { "id": 5, "name": "Company E" }
  ],
  "totalElements": 100,  // ← Tổng số công ty
  "totalPages": 20,       // ← Tổng số trang (100/5=20)
  "number": 0,            // ← Trang hiện tại
  "size": 5,              // ← Số công ty mỗi trang
  "hasNext": true         // ← Có trang tiếp không?
}
```

#### Phương Pháp Truy Cập Dữ Liệu Từ Page

```java
// Frontend nhận Page object
Page<Company> result = getCompanies(0, 5);

// Lấy chỉ dữ liệu (5 công ty)
List<Company> companies = result.getContent();

// Lấy tổng số công ty
long total = result.getTotalElements();  // 100

// Lấy tổng số trang
int totalPages = result.getTotalPages();  // 20

// Kiểm tra có trang tiếp không
if(result.hasNext()) {
    // Tải trang tiếp theo
    Page<Company> nextPage = getCompanies(1, 5);
}

// Kiểm tra có trang trước không
if(result.hasPrevious()) {
    // Tải trang trước
    Page<Company> prevPage = getCompanies(result.getNumber() - 1, 5);
}
```

#### Sự Khác Biệt Rõ Ràng Nhất

| | **Pageable** | **Page** |
|---|---|---|
| **Khi nào dùng** | Khi **gửi** yêu cầu lên server | Khi **nhận** kết quả từ server |
| **Chứa gì** | Trang số, kích thước trang, cách sắp xếp | Dữ liệu thực + thông tin tổng |
| **Ví dụ** | `PageRequest.of(0, 5)` | Danh sách 5 công ty + "có 100 công ty tổng cộng" |
| **Có dữ liệu không** | ❌ Không, chỉ có "hướng dẫn" | ✅ Có, là dữ liệu thực tế |
| **Mục đích** | Yêu cầu phân trang | Kết quả phân trang |

**Tóm lại:** 
- **Pageable** = "Cái bạn **GỬI**" (yêu cầu)
- **Page** = "Cái bạn **NHẬN ĐƯỢC**" (kết quả)

#### Sơ Đồ So Sánh Pageable vs Page

```
┌─────────────────────────────────────────────────────────────┐
│                    PAGEABLE (INPUT)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Yêu cầu của Client:                                 │  │
│  │  "Cho tôi trang 0, mỗi trang 5 sản phẩm"            │  │
│  │                                                       │  │
│  │  Pageable pageable = PageRequest.of(0, 5);          │  │
│  │                                                       │  │
│  │  Chứa:                                               │  │
│  │  • pageNumber = 0                                    │  │
│  │  • pageSize = 5                                      │  │
│  │  • sort = null                                       │  │
│  │                                                       │  │
│  │  ⚠️ KHÔNG có dữ liệu thực tế                         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Gửi xuống Database
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE                                 │
│  Thực thi query với Pageable:                              │
│  SELECT * FROM companies LIMIT 5 OFFSET 0;                 │
│  SELECT COUNT(*) FROM companies;                           │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Trả về kết quả
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    PAGE (OUTPUT)                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Kết quả từ Database:                                │  │
│  │  "Đây là 5 sản phẩm trang đầu, có tổng 100 sản phẩm" │  │
│  │                                                       │  │
│  │  Page<Company> page = ...;                           │  │
│  │                                                       │  │
│  │  Chứa:                                               │  │
│  │  • content = [Company, Company, ...] (5 items)      │  │
│  │  • totalElements = 100                               │  │
│  │  • totalPages = 20                                   │  │
│  │  • number = 0                                        │  │
│  │  • size = 5                                          │  │
│  │  • hasNext = true                                    │  │
│  │                                                       │  │
│  │  ✅ CÓ dữ liệu thực tế                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 3. Specification là gì?

**Specification** là một interface trong Spring Data JPA để xây dựng các điều kiện query động một cách type-safe.

#### Định nghĩa:

```java
public interface Specification<T> {
    Predicate toPredicate(
        Root<T> root,              // Root entity (Company)
        CriteriaQuery<?> query,     // CriteriaQuery để build
        CriteriaBuilder cb         // CriteriaBuilder để tạo Predicate
    );
}
```

#### Cách hoạt động:

Specification sử dụng **JPA Criteria API** để build query động:
- **Root<T>**: Đại diện cho entity gốc (Company)
- **CriteriaQuery**: Query đang được build
- **CriteriaBuilder**: Builder để tạo các điều kiện (Predicate)

#### Ví dụ Specification thủ công:

```java
// Tạo Specification để tìm company có name = "ABC"
Specification<Company> spec = (root, query, cb) -> 
    cb.equal(root.get("name"), "ABC");

// Sử dụng
Page<Company> page = repository.findAll(spec, pageable);
```

### 4. Luồng xử lý chi tiết khi có Specification

Khi client gửi request với filter:
```
GET /api/v1/company?filter=name:'ABC'&page=0&size=10
```

#### BƯỚC 1: Spring MVC nhận Request

```
┌─────────────────────────────────────────────────────────────┐
│ DispatcherServlet nhận HTTP Request                         │
│ - Parse URL: /api/v1/company                                │
│ - Parse Query Parameters:                                   │
│   • filter = "name:'ABC'"                                   │
│   • page = "0"                                              │
│   • size = "10"                                             │
└─────────────────────────────────────────────────────────────┘
```

#### BƯỚC 2: @Filter Annotation xử lý

```
┌─────────────────────────────────────────────────────────────┐
│ SpringFilter Handler Method Argument Resolver               │
│                                                             │
│ 1. Phát hiện @Filter annotation                            │
│ 2. Đọc query parameter "filter" = "name:'ABC'"            │
│ 3. Gọi FilterParser để parse string                        │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ FilterParser.parse("name:'ABC'")                           │
│                                                             │
│ Parse thành AST (Abstract Syntax Tree):                    │
│                                                             │
│        ┌──────────────┐                                    │
│        │   EQUALS     │                                    │
│        ├──────┬───────┤                                    │
│        │ name │ 'ABC' │                                    │
│        └──────┴───────┘                                    │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ AST → Specification Converter                              │
│                                                             │
│ Convert AST thành Specification<Company>:                   │
│                                                             │
│ Specification<Company> spec = (root, query, cb) -> {      │
│     Path<String> namePath = root.get("name");              │
│     return cb.equal(namePath, "ABC");                      │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

#### BƯỚC 3: Pageable được tạo tự động

```
┌─────────────────────────────────────────────────────────────┐
│ Spring Data Web Support                                     │
│                                                             │
│ Parse query parameters:                                     │
│ - page=0 → pageNumber = 0                                  │
│ - size=10 → pageSize = 10                                  │
│                                                             │
│ Tạo Pageable:                                              │
│ Pageable pageable = PageRequest.of(0, 10);                │
└─────────────────────────────────────────────────────────────┘
```

#### BƯỚC 4: Controller nhận Specification và Pageable

```java
@GetMapping("/company")
public ResponseEntity<ResultPaginationResponse<CompanyDTO>> getAllCompany(
        @Filter Specification<Company> spec,    // ← Đã được parse từ filter
        Pageable pageable                       // ← Đã được tạo từ page, size
) {
    return ResponseEntity.ok(companyService.getAllCompany(pageable, spec));
}
```

#### BƯỚC 5: Service Layer xử lý

```java
public ResultPaginationResponse<CompanyDTO> getAllCompany(
    Pageable pageable, 
    Specification<Company> spec
) {
    Page<Company> companyPage;
    
    // ⭐ KIỂM TRA SPEC
    if (spec != null) {
        // ⭐ CÓ SPEC → GỌI findAll(spec, pageable)
        companyPage = this.companyRespository.findAll(spec, pageable);
    } else {
        // KHÔNG CÓ SPEC → GỌI findAll(pageable)
        companyPage = this.companyRespository.findAll(pageable);
    }
    
    return ResultPaginationResponse.ok(companyPage, companyMapper::toDto);
}
```

#### BƯỚC 6: Repository Layer - JpaSpecificationExecutor

Khi gọi `repository.findAll(spec, pageable)`, các bước sau được thực hiện:

```
┌─────────────────────────────────────────────────────────────┐
│ JpaSpecificationExecutor.findAll(Specification, Pageable)   │
│                                                             │
│ 1. Tạo EntityManager                                       │
│ 2. Tạo CriteriaBuilder từ EntityManager                   │
│ 3. Tạo CriteriaQuery<Company>                             │
│ 4. Tạo Root<Company> từ CriteriaQuery                      │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Gọi spec.toPredicate(root, query, cb)                      │
│                                                             │
│ Specification thực thi:                                    │
│   Predicate predicate = cb.equal(                          │
│       root.get("name"),                                     │
│       "ABC"                                                 │
│   );                                                        │
│                                                             │
│ query.where(predicate);  // Thêm WHERE clause              │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Apply Pagination                                            │
│                                                             │
│ TypedQuery<Company> typedQuery =                           │
│     entityManager.createQuery(query);                      │
│                                                             │
│ // Tính toán offset và limit                               │
│ int offset = pageable.getOffset();  // 0 * 10 = 0          │
│ int limit = pageable.getPageSize();  // 10                  │
│                                                             │
│ typedQuery.setFirstResult(offset);  // OFFSET 0             │
│ typedQuery.setMaxResults(limit);    // LIMIT 10            │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Execute Query - COUNT                                       │
│                                                             │
│ // Tạo COUNT query để tính total                           │
│ CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);│
│ Root<Company> countRoot = countQuery.from(Company.class);  │
│ countQuery.select(cb.count(countRoot));                     │
│                                                             │
│ // Apply cùng predicate                                    │
│ Predicate countPredicate = spec.toPredicate(               │
│     countRoot, countQuery, cb                              │
│ );                                                          │
│ countQuery.where(countPredicate);                           │
│                                                             │
│ Long total = entityManager                                 │
│     .createQuery(countQuery)                               │
│     .getSingleResult();  // Tổng số bản ghi                │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Execute Query - SELECT                                     │
│                                                             │
│ List<Company> content = typedQuery.getResultList();        │
│                                                             │
│ // SQL được tạo ra:                                        │
│ SELECT * FROM companies                                     │
│ WHERE name = 'ABC'                                         │
│ LIMIT 10 OFFSET 0                                          │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Tạo Page<Company> object                                   │
│                                                             │
│ Page<Company> page = new PageImpl<>(                       │
│     content,        // List<Company> - 10 records          │
│     pageable,       // Pageable                            │
│     total           // long - Tổng số (ví dụ: 25)         │
│ );                                                          │
│                                                             │
│ // Page object chứa:                                       │
│ // - content: 10 Company entities                          │
│ // - totalElements: 25                                    │
│ // - totalPages: 3 (25 / 10 = 2.5 → 3)                    │
│ // - number: 0                                             │
│ // - size: 10                                              │
└─────────────────────────────────────────────────────────────┘
```

#### BƯỚC 7: Service convert Page sang Response

```java
return ResultPaginationResponse.ok(companyPage, companyMapper::toDto);
```

```
┌─────────────────────────────────────────────────────────────┐
│ ResultPaginationResponse.ok()                              │
│                                                             │
│ 1. Tạo MetaResponse:                                       │
│    meta.setPage(page.getNumber() + 1);      // 0 + 1 = 1   │
│    meta.setPageSize(page.getSize());        // 10          │
│    meta.setTotal(page.getTotalElements());   // 25          │
│    meta.setPages(page.getTotalPages());     // 3           │
│                                                             │
│ 2. Convert content sang DTO:                               │
│    List<CompanyDTO> dtos = page.getContent()               │
│        .stream()                                           │
│        .map(companyMapper::toDto)                          │
│        .toList();                                          │
│                                                             │
│ 3. Tạo ResultPaginationResponse:                           │
│    {                                                        │
│      "meta": { page: 1, pageSize: 10, total: 25, pages: 3 },│
│      "result": [CompanyDTO, CompanyDTO, ...]                │
│    }                                                        │
└─────────────────────────────────────────────────────────────┘
```

### 5. So sánh: Có Spec vs Không có Spec

#### Trường hợp 1: CÓ Specification

**Request:**
```
GET /api/v1/company?filter=name:'ABC'&page=0&size=10
```

**Luồng xử lý:**
```
1. @Filter parse "name:'ABC'" → Specification<Company>
2. Service: spec != null → TRUE
3. Repository: findAll(spec, pageable)
4. JPA tạo query: SELECT * FROM companies WHERE name = 'ABC' LIMIT 10 OFFSET 0
5. Execute COUNT query: SELECT COUNT(*) FROM companies WHERE name = 'ABC'
6. Trả về Page với 10 records và total = số lượng thỏa mãn filter
```

**SQL được thực thi:**
```sql
-- Query chính
SELECT * FROM companies 
WHERE name = 'ABC' 
LIMIT 10 OFFSET 0;

-- Query đếm (để tính total)
SELECT COUNT(*) FROM companies 
WHERE name = 'ABC';
```

#### Trường hợp 2: KHÔNG có Specification

**Request:**
```
GET /api/v1/company?page=0&size=10
```

**Luồng xử lý:**
```
1. @Filter không có giá trị → spec = null
2. Service: spec != null → FALSE
3. Repository: findAll(pageable)
4. JPA tạo query: SELECT * FROM companies LIMIT 10 OFFSET 0
5. Execute COUNT query: SELECT COUNT(*) FROM companies
6. Trả về Page với 10 records và total = tổng số records trong DB
```

**SQL được thực thi:**
```sql
-- Query chính
SELECT * FROM companies 
LIMIT 10 OFFSET 0;

-- Query đếm (để tính total)
SELECT COUNT(*) FROM companies;
```

### 6. Các class và interface liên quan

#### Spring Data JPA:

- **`Pageable`**: Interface đại diện cho phân trang
- **`PageRequest`**: Implementation của Pageable
- **`Page<T>`**: Interface đại diện cho một trang dữ liệu
- **`PageImpl<T>`**: Implementation của Page
- **`Specification<T>`**: Interface để build query động
- **`JpaSpecificationExecutor<T>`**: Interface cung cấp method findAll(Specification, Pageable)

#### SpringFilter:

- **`@Filter`**: Annotation để parse query parameter thành Specification
- **`FilterParser`**: Class parse filter string thành AST
- **`SpecificationConverter`**: Class convert AST thành Specification

### 7. Performance Considerations

#### Khi có Specification:

1. **COUNT Query**: Luôn được thực thi để tính `totalElements`
   - Nếu có filter phức tạp, COUNT query có thể chậm
   - Có thể tối ưu bằng index trên các field filter

2. **SELECT Query**: Chỉ lấy dữ liệu trong trang hiện tại
   - Sử dụng LIMIT và OFFSET
   - Chỉ load dữ liệu cần thiết

3. **Memory**: Chỉ load `pageSize` records vào memory

#### Tối ưu hóa:

- **Index**: Tạo index trên các field thường xuyên filter
- **Pagination**: Luôn sử dụng pagination để giới hạn số lượng records
- **Projection**: Có thể sử dụng DTO projection để giảm dữ liệu load

### 8. Ví dụ minh họa đầy đủ

**Request:**
```
GET /api/v1/company?filter=name:contains:'Tech' and id:>10&page=1&size=5&sort=name,asc
```

**Các bước xử lý:**

1. **Parse Filter:**
   ```java
   Specification<Company> spec = (root, query, cb) -> {
       Predicate namePredicate = cb.like(
           root.get("name"), 
           "%Tech%"
       );
       Predicate idPredicate = cb.greaterThan(
           root.get("id"), 
           10L
       );
       return cb.and(namePredicate, idPredicate);
   };
   ```

2. **Tạo Pageable:**
   ```java
   Pageable pageable = PageRequest.of(
       1,                              // page 1 (0-based)
       5,                              // size 5
       Sort.by("name").ascending()     // sort by name ASC
   );
   ```

3. **Execute Query:**
   ```sql
   SELECT * FROM companies 
   WHERE name LIKE '%Tech%' AND id > 10 
   ORDER BY name ASC 
   LIMIT 5 OFFSET 5;
   ```

4. **Execute Count:**
   ```sql
   SELECT COUNT(*) FROM companies 
   WHERE name LIKE '%Tech%' AND id > 10;
   ```

5. **Result:**
   ```java
   Page<Company> page = new PageImpl<>(
       content,    // 5 Company entities
       pageable,   // PageRequest(page=1, size=5, sort=name ASC)
       total       // Tổng số thỏa mãn filter
   );
   ```

## 🏗️ Kiến trúc Code

### 1. Controller Layer

```java
@GetMapping("/company")
public ResponseEntity<ResultPaginationResponse<CompanyDTO>> getAllCompany(
        @Filter Specification<Company> spec,
        Pageable pageable
) {
    return ResponseEntity.ok(companyService.getAllCompany(pageable, spec));
}
```

**Chức năng:**
- Nhận request từ client
- `@Filter` tự động parse query parameter `filter` thành `Specification<Company>`
- `Pageable` tự động parse các tham số `page`, `size`, `sort`

### 2. Service Layer

```java
public ResultPaginationResponse<CompanyDTO> getAllCompany(
    Pageable pageable, 
    Specification<Company> spec
) {
    Page<Company> companyPage;
    
    if (spec != null) {
        companyPage = this.companyRespository.findAll(spec, pageable);
    } else {
        companyPage = this.companyRespository.findAll(pageable);
    }
    
    return ResultPaginationResponse.ok(companyPage, companyMapper::toDto);
}
```

**Chức năng:**
- Kiểm tra xem có filter hay không
- Gọi repository với hoặc không có Specification
- Convert kết quả sang DTO và trả về

### 3. Repository Layer

```java
public interface CompanyRespository extends 
    JpaRepository<Company, Long>,
    JpaSpecificationExecutor<Company> {
}
```

**Chức năng:**
- `JpaSpecificationExecutor` cung cấp method `findAll(Specification, Pageable)`
- Tự động build SQL từ Specification
- Thực thi query với pagination

## 🔒 Bảo mật

### Authentication

Tất cả các endpoint filter đều yêu cầu **JWT Token** trong header:

```
Authorization: Bearer {your_jwt_token}
```

### SQL Injection Protection

SpringFilter và JPA Specification tự động bảo vệ khỏi SQL Injection:
- Sử dụng PreparedStatement
- Parameter binding tự động
- Không cho phép SQL thô trong filter

## ⚠️ Lưu ý quan trọng

### 1. Encoding URL

Khi filter có khoảng trắng hoặc ký tự đặc biệt, cần encode URL:

**Ví dụ:**
```
Filter: name:'ABC Company'
URL: filter=name:'ABC%20Company'
```

### 2. Trường có thể Filter

Chỉ các trường trong Entity `Company` mới có thể filter:
- `id` (Long)
- `name` (String)
- `description` (String)
- `address` (String)
- `logo` (String)
- `createdDate` (Instant)
- `updatedAt` (Instant)
- `createdBy` (String)
- `updatedBy` (String)

### 3. Case Sensitivity

Filter chuỗi trong MySQL mặc định **không phân biệt hoa thường** (case-insensitive).

### 4. Performance

- Filter phức tạp có thể ảnh hưởng đến performance
- Nên sử dụng index cho các trường thường xuyên filter
- Pagination giúp giảm tải cho database

### 5. Giới hạn

- Hiện tại chỉ áp dụng cho Entity `Company`
- Chưa hỗ trợ filter cho Entity `User`
- Không hỗ trợ filter nested object (quan hệ)

## 🧪 Testing với cURL

### Ví dụ 1: Filter đơn giản

```bash
curl -X GET "http://localhost:8080/api/v1/company?filter=name:'ABC'" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Ví dụ 2: Filter với pagination

```bash
curl -X GET "http://localhost:8080/api/v1/company?filter=name:contains:'Tech'&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Ví dụ 3: Filter phức tạp

```bash
curl -X GET "http://localhost:8080/api/v1/company?filter=(name:contains:'Tech'%20or%20name:contains:'IT')%20and%20address:isNotNull" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📖 Tài liệu tham khảo

- [SpringFilter Documentation](https://github.com/turkraft/springfilter)
- [Spring Data JPA Specification](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- [Spring Data Pagination](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.query-creation)

## 🐛 Troubleshooting

### Lỗi: Filter không hoạt động

**Nguyên nhân:**
- Query parameter `filter` không đúng cú pháp
- Repository chưa extends `JpaSpecificationExecutor`

**Giải pháp:**
- Kiểm tra cú pháp filter
- Đảm bảo Repository extends `JpaSpecificationExecutor<Company>`

### Lỗi: Không tìm thấy field

**Nguyên nhân:**
- Field không tồn tại trong Entity
- Tên field sai chính tả

**Giải pháp:**
- Kiểm tra tên field trong Entity `Company`
- Sử dụng đúng tên field (case-sensitive)

### Lỗi: 401 Unauthorized

**Nguyên nhân:**
- Thiếu JWT Token
- Token không hợp lệ hoặc hết hạn

**Giải pháp:**
- Thêm header `Authorization: Bearer {token}`
- Đăng nhập lại để lấy token mới

---

**Tác giả:** TrinhNV  
**Ngày cập nhật:** 2024  
**Phiên bản:** 1.0

