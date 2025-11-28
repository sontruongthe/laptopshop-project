# 🎉 HƯỚNG DẪN TÍCH HỢP THANH TOÁN VNPAY - T-MART E-COMMERCE

## 📋 Tổng Quan

Dự án của bạn đang sử dụng **MVC Pattern với 5-layer Architecture**:
- **Controller Layer** - Xử lý HTTP requests
- **Service Layer** - Business logic
- **DAO Layer** - Data Access với JPA
- **Domain Layer** - Entity models
- **View Layer** - Frontend (AngularJS + Thymeleaf)

Đã tích hợp **VNPay Payment Gateway** để thanh toán online.

---

## 🚀 CÁC FILE ĐÃ TẠO

### 1. Backend (Java Spring Boot)

#### **Domain Layer**
- ✅ `Payment.java` - Entity lưu thông tin giao dịch thanh toán

#### **DAO Layer**
- ✅ `PaymentDAO.java` - Repository với các query tùy chỉnh

#### **Service Layer**
- ✅ `PaymentService.java` - Interface service
- ✅ `PaymentServiceImpl.java` - Implementation với VNPay integration

#### **Controller Layer**
- ✅ `PaymentController.java` - Xử lý redirect từ VNPay
- ✅ `PaymentRestController.java` - REST API endpoints

#### **Config Layer**
- ✅ `VNPayConfig.java` - Cấu hình VNPay parameters

### 2. Frontend (AngularJS + HTML)

- ✅ `order.js` - Updated với payment method selection
- ✅ `payment-result.html` - View hiển thị kết quả thanh toán

### 3. Database

- ✅ `payment_table.sql` - Script tạo bảng Payments + Views + Stored Procedures

### 4. Configuration

- ✅ `application.properties` - Thêm VNPay config

---

## 📊 DATABASE SETUP

### Chạy SQL Script

```powershell
# Mở SQL Server Management Studio (SSMS)
# Connect vào server: DELL
# Chọn database: asmJava6
# Mở file: payment_table.sql
# Execute (F5)
```

Hoặc chạy qua PowerShell:

```powershell
sqlcmd -S DELL -d asmJava6 -i "f:\BTL_HDV_Final-master\AsignmentJava6\AsignmentJava6\payment_table.sql"
```

### Bảng `Payments` bao gồm:

| Column | Type | Description |
|--------|------|-------------|
| paymentID | BIGINT (PK) | ID tự động tăng |
| orderID | INT (FK) | Liên kết với Orders |
| paymentMethod | NVARCHAR(50) | COD, VNPAY, MOMO |
| transactionID | NVARCHAR(100) | Mã giao dịch VNPay |
| amount | BIGINT | Số tiền (VNĐ) |
| paymentStatus | NVARCHAR(50) | PENDING, SUCCESS, FAILED |
| paymentDate | DATETIME | Thời gian thanh toán |
| bankCode | NVARCHAR(50) | Mã ngân hàng |
| responseCode | NVARCHAR(10) | Response code VNPay |
| description | NVARCHAR(500) | Mô tả |

---

## ⚙️ CONFIGURATION

### VNPay Test Account (Sandbox)

File: `application.properties`

```properties
# VNPay Sandbox (Môi trường test)
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay-return
vnpay.tmnCode=CGVDEMO
vnpay.hashSecret=RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ
```

### Test Cards (Dùng trên Sandbox)

| Ngân hàng | Số thẻ | Họ tên | Ngày phát hành | OTP |
|-----------|--------|--------|----------------|-----|
| NCB | 9704198526191432198 | NGUYEN VAN A | 07/15 | 123456 |
| VCB | 9704229209080700506 | NGUYEN VAN B | 11/17 | OTP SMS |

---

## 🔄 LUỒNG THANH TOÁN

### Luồng COD (Cash on Delivery)

```
1. User nhập địa chỉ + SĐT
2. Click "Order" → Chọn "Thanh toán khi nhận hàng (COD)"
3. Frontend call: POST /payment/create-cod
   {
     "orderID": 123,
     "amount": 15000000
   }
4. Backend tạo Payment record với status PENDING
5. Gửi email xác nhận
6. Redirect về /order (Trang đơn hàng)
```

### Luồng VNPay

```
1. User nhập địa chỉ + SĐT
2. Click "Order" → Chọn "Thanh toán VNPay"
3. Frontend call: POST /payment/create-vnpay-url
   {
     "orderID": 123,
     "amount": 15000000,
     "orderInfo": "Thanh toan don hang #123"
   }
4. Backend tạo Payment record + VNPay URL
5. Frontend redirect đến VNPay
6. User thanh toán tại VNPay
7. VNPay redirect về: /payment/vnpay-return?vnp_ResponseCode=00&...
8. Backend verify signature → Update payment status
9. Hiển thị payment-result.html
```

---

## 🛠️ API ENDPOINTS

### Payment REST APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payment/create-vnpay-url` | Tạo URL thanh toán VNPay |
| POST | `/payment/create-cod` | Tạo payment COD |
| GET | `/payment/check-paid/{orderID}` | Kiểm tra order đã thanh toán |
| GET | `/payment/by-order/{orderID}` | Lấy danh sách payments của order |
| PUT | `/payment/cancel/{paymentID}` | Hủy payment |

### Payment Controller

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/payment/vnpay-return` | VNPay callback URL |

---

## 🧪 TESTING

### 1. Test COD Payment

```javascript
// Trên trang /cart
1. Thêm sản phẩm vào giỏ hàng
2. Nhập địa chỉ + số điện thoại
3. Click "Order"
4. Chọn "Thanh toán khi nhận hàng (COD)"
5. Kiểm tra:
   - Đơn hàng được tạo
   - Email xác nhận được gửi
   - Payment record có status "PENDING"
   - Redirect về trang /order
```

### 2. Test VNPay Payment

```javascript
// Trên trang /cart
1. Thêm sản phẩm vào giỏ hàng
2. Nhập địa chỉ + số điện thoại
3. Click "Order"
4. Chọn "Thanh toán VNPay"
5. Redirect đến VNPay sandbox
6. Chọn ngân hàng NCB
7. Nhập thông tin test card:
   - Số thẻ: 9704198526191432198
   - Họ tên: NGUYEN VAN A
   - Ngày phát hành: 07/15
   - OTP: 123456
8. Kiểm tra:
   - Redirect về /payment/vnpay-return
   - Hiển thị kết quả thành công
   - Payment status = "SUCCESS"
   - Order status = 2 (Đã xác nhận)
```

### 3. Test Payment APIs với Postman

```bash
# 1. Tạo VNPay URL
POST http://localhost:8080/payment/create-vnpay-url
Content-Type: application/json

{
  "orderID": 1,
  "amount": 15000000,
  "orderInfo": "Thanh toan don hang #1"
}

# 2. Tạo COD Payment
POST http://localhost:8080/payment/create-cod
Content-Type: application/json

{
  "orderID": 1,
  "amount": 15000000
}

# 3. Kiểm tra payment của order
GET http://localhost:8080/payment/by-order/1
```

---

## 🔍 TROUBLESHOOTING

### Lỗi thường gặp:

#### 1. **"Payment không tồn tại"**
- Kiểm tra orderID có tồn tại trong DB
- Xem log console để kiểm tra request data

#### 2. **"Invalid signature"**
- Kiểm tra `vnpay.hashSecret` trong application.properties
- Đảm bảo dùng đúng hash secret của sandbox

#### 3. **VNPay redirect về lỗi**
- Kiểm tra `vnpay.returnUrl` có đúng không
- Xem `vnp_ResponseCode` để biết lỗi cụ thể:
  - `00` = Thành công
  - `24` = Khách hàng hủy giao dịch
  - `51` = Tài khoản không đủ tiền
  - ...

#### 4. **Email không gửi được**
- Kiểm tra `MAIL_USERNAME` và `MAIL_PASSWORD`
- Enable "Less secure app access" trên Gmail

---

## 🌐 PRODUCTION DEPLOYMENT

### Khi deploy lên production:

1. **Đăng ký VNPay Account thật**
   - Website: https://vnpay.vn
   - Liên hệ sales để được cấp:
     - `TMN_CODE` (Merchant Code)
     - `HASH_SECRET` (Secret Key)

2. **Update application.properties**
```properties
# Production VNPay
vnpay.url=https://vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=https://yourdomain.com/payment/vnpay-return
vnpay.tmnCode=${VNPAY_TMN_CODE}
vnpay.hashSecret=${VNPAY_HASH_SECRET}
vnpay.apiUrl=https://vnpayment.vn/merchant_webapi/api/transaction
```

3. **Set Environment Variables**
```bash
export VNPAY_TMN_CODE=your_real_tmn_code
export VNPAY_HASH_SECRET=your_real_hash_secret
```

4. **SSL Certificate**
   - VNPay yêu cầu HTTPS cho production
   - Cài SSL certificate cho domain

---

## 📈 MONITORING & ANALYTICS

### Queries hữu ích:

```sql
-- Xem thống kê thanh toán
SELECT * FROM vw_PaymentStatistics;

-- Xem payment history của user
EXEC sp_GetUserPaymentHistory @username = 'user01';

-- Thống kê doanh thu theo phương thức
EXEC sp_RevenueByPaymentMethod 
    @fromDate = '2025-11-01', 
    @toDate = '2025-11-30';

-- Top 10 giao dịch thành công gần nhất
SELECT TOP 10 * 
FROM vw_PaymentDetails 
WHERE paymentStatus = 'SUCCESS'
ORDER BY paymentDate DESC;
```

---

## ✅ CHECKLIST

- [x] Tạo Payment entity
- [x] Tạo PaymentDAO
- [x] Implement PaymentService với VNPay integration
- [x] Tạo PaymentRestController (API)
- [x] Tạo PaymentController (View)
- [x] Tạo payment-result.html
- [x] Update order.js với payment method selection
- [x] Tạo payment_table.sql
- [x] Update application.properties
- [ ] **Chạy SQL script tạo bảng Payments**
- [ ] **Test COD payment**
- [ ] **Test VNPay payment với sandbox**
- [ ] **Đăng ký VNPay production account**

---

## 📞 SUPPORT

### VNPay Support:
- Hotline: 1900 55 55 77
- Email: support@vnpay.vn
- Docs: https://sandbox.vnpayment.vn/apis/

### Test Environment:
- Sandbox URL: https://sandbox.vnpayment.vn
- Merchant portal: https://sandbox.vnpayment.vn/merchantv2/

---

## 🎯 NEXT STEPS

1. **Chạy SQL script**
```powershell
cd "f:\BTL_HDV_Final-master\AsignmentJava6\AsignmentJava6"
sqlcmd -S DELL -d asmJava6 -i payment_table.sql
```

2. **Restart Spring Boot application**
```powershell
# Stop hiện tại (Ctrl+C)
# Chạy lại
mvn spring-boot:run
```

3. **Test thanh toán**
- Vào http://localhost:8080
- Thêm sản phẩm vào giỏ hàng
- Checkout và test cả COD & VNPay

4. **Kiểm tra database**
```sql
SELECT * FROM Payments;
```

---

**Chúc mừng! 🎉 Bạn đã tích hợp thành công VNPay Payment Gateway vào T-MART E-Commerce!**
