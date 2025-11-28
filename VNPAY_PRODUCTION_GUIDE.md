# 🚀 HƯỚNG DẪN DEPLOY VNPAY LÊN PRODUCTION

## 📋 **CHECKLIST TRƯỚC KHI DEPLOY**

### ✅ **Bước 1: Đăng ký VNPay chính thức**

1. **Liên hệ VNPay:**
   - Website: https://vnpay.vn/
   - Hotline: 1900 55 55 77
   - Email: support@vnpay.vn

2. **Chuẩn bị hồ sơ:**
   - ✅ Giấy phép kinh doanh
   - ✅ Thông tin doanh nghiệp
   - ✅ Tài khoản ngân hàng doanh nghiệp
   - ✅ Website đã deploy (có SSL - HTTPS)

3. **Thời gian xử lý:** 3-7 ngày làm việc

4. **Chi phí:**
   - Phí setup: Miễn phí - 5 triệu VNĐ (tùy gói)
   - Phí giao dịch: 2-3% mỗi transaction
   - Phí rút tiền về tài khoản: 0.5-1%

---

### ✅ **Bước 2: Nhận thông tin từ VNPay**

Sau khi đăng ký thành công, VNPay sẽ cung cấp:

```
┌─────────────────────────────────────────┐
│  THÔNG TIN MERCHANT PRODUCTION          │
├─────────────────────────────────────────┤
│  Terminal ID (vnp_TmnCode):             │
│  → Ví dụ: ABCD1234                      │
│                                         │
│  Hash Secret Key (vnp_HashSecret):      │
│  → Ví dụ: XXXXXXXXXXXXXXXXXXXXX         │
│                                         │
│  API URL:                               │
│  → https://vnpay.vn/                    │
└─────────────────────────────────────────┘
```

⚠️ **LƯU Ý:** Bảo mật tuyệt đối 2 thông tin này!

---

### ✅ **Bước 3: Cập nhật code**

#### **3.1. Cấu hình application.properties**

File này đã được cấu hình sẵn cho production. Bạn chỉ cần set **Environment Variables** trên server:

```bash
# Trên Linux/Mac
export VNPAY_TMN_CODE="YOUR_REAL_TMN_CODE"
export VNPAY_HASH_SECRET="YOUR_REAL_HASH_SECRET"
export VNPAY_RETURN_URL="https://yourdomain.com/payment/vnpay-return"

# Trên Windows Server
setx VNPAY_TMN_CODE "YOUR_REAL_TMN_CODE"
setx VNPAY_HASH_SECRET "YOUR_REAL_HASH_SECRET"
setx VNPAY_RETURN_URL "https://yourdomain.com/payment/vnpay-return"
```

#### **3.2. Hoặc dùng file .env (không khuyến khích production)**

```properties
# .env
VNPAY_TMN_CODE=ABCD1234
VNPAY_HASH_SECRET=XXXXXXXXXXXXXXXXXX
VNPAY_RETURN_URL=https://yourdomain.com/payment/vnpay-return
```

⚠️ **QUAN TRỌNG:** Add `.env` vào `.gitignore`!

---

### ✅ **Bước 4: Cấu hình domain và SSL**

1. **Mua domain:** yourdomain.com
2. **Cài SSL certificate** (HTTPS bắt buộc)
   - Let's Encrypt (miễn phí)
   - Hoặc SSL trả phí

3. **Cập nhật returnUrl:**
```properties
vnpay.returnUrl=https://yourdomain.com/payment/vnpay-return
```

4. **Đăng ký URL với VNPay:**
   - Login vào VNPay portal
   - Thêm domain vào whitelist
   - Cập nhật IPN URL (callback URL)

---

### ✅ **Bước 5: Test trên production**

#### **5.1. Test với số tiền nhỏ (10,000 VNĐ)**

```bash
# Test flow đầy đủ:
1. Tạo đơn hàng → Chọn VNPay
2. Thanh toán với thẻ test (VNPay cung cấp)
3. Kiểm tra callback có về đúng không
4. Kiểm tra database có lưu payment không
5. Kiểm tra email thông báo
```

#### **5.2. Thẻ test VNPay cung cấp:**

```
Ngân hàng: NCB
Số thẻ: 9704198526191432198
Tên chủ thẻ: NGUYEN VAN A
Ngày phát hành: 07/15
Mật khẩu OTP: 123456
```

---

### ✅ **Bước 6: Monitoring và bảo mật**

#### **6.1. Logging**

Thêm log để tracking:

```java
@Override
public String createVNPayPaymentUrl(...) {
    logger.info("Creating VNPay payment for orderID: {}, amount: {}", orderID, amount);
    
    // ... existing code ...
    
    logger.info("VNPay URL created: {}", paymentUrl);
    return paymentUrl;
}
```

#### **6.2. Error handling**

Đảm bảo xử lý đầy đủ các error codes:

```java
// Xem chi tiết error codes tại:
// https://sandbox.vnpayment.vn/apis/docs/bang-ma-loi/
```

#### **6.3. Bảo mật**

- ✅ HTTPS bắt buộc
- ✅ Validate signature từ VNPay
- ✅ Check IP whitelist (nếu có)
- ✅ Rate limiting (chống spam)
- ✅ Không log sensitive data (tmnCode, hashSecret)

---

## 🔄 **SO SÁNH SANDBOX VS PRODUCTION**

| Tiêu chí | Sandbox (Test) | Production (Thật) |
|----------|----------------|-------------------|
| **URL** | sandbox.vnpayment.vn | vnpay.vn |
| **Đăng ký** | Tự động, miễn phí | Qua VNPay, có phí |
| **Thẻ test** | Dùng thẻ giả | Thẻ thật |
| **Giao dịch** | Không rút tiền thật | Rút tiền thật |
| **Hỗ trợ** | Tài liệu online | Support 24/7 |
| **SSL** | Không bắt buộc | BẮT BUỘC HTTPS |

---

## 📝 **DEPLOYMENT CHECKLIST**

```
☐ Đăng ký VNPay merchant account
☐ Nhận Terminal ID và Hash Secret
☐ Cấu hình environment variables
☐ Mua domain và cài SSL
☐ Update returnUrl trong code
☐ Đăng ký domain với VNPay portal
☐ Test với thẻ test
☐ Setup logging và monitoring
☐ Thêm error handling đầy đủ
☐ Review bảo mật
☐ Test với transaction thật (nhỏ)
☐ Go live!
```

---

## 🆘 **TROUBLESHOOTING**

### **Lỗi "Không tìm thấy website" (Code 72)**
- ✅ Check `vnp_TmnCode` đúng chưa
- ✅ Domain đã đăng ký với VNPay chưa
- ✅ SSL đã cài chưa (HTTPS)

### **Lỗi signature không hợp lệ (Code 97)**
- ✅ Check `vnp_HashSecret` đúng chưa
- ✅ Check encoding UTF-8
- ✅ Check thứ tự params khi hash

### **Callback không về**
- ✅ Check `vnp_ReturnUrl` accessible từ internet
- ✅ Check firewall/security group
- ✅ Check server logs

---

## 📞 **HỖ TRỢ**

**VNPay Support:**
- Hotline: 1900 55 55 77
- Email: support@vnpay.vn
- Portal: https://vnpay.vn/

**Documentation:**
- API Docs: https://sandbox.vnpayment.vn/apis/docs/
- Error Codes: https://sandbox.vnpayment.vn/apis/docs/bang-ma-loi/

---

## ✅ **KẾT LUẬN**

### **Để deploy lên production:**

1. **Đăng ký VNPay chính thức** (3-7 ngày)
2. **Nhận credentials** (tmnCode, hashSecret)
3. **Set environment variables** trên server
4. **Cài SSL** (HTTPS bắt buộc)
5. **Test kỹ** trước khi go live
6. **Monitor** sau khi deploy

### **Chi phí ước tính:**

- VNPay setup: 0-5 triệu VNĐ
- SSL: 0-2 triệu VNĐ/năm
- Domain: 200k-500k VNĐ/năm
- Transaction fee: 2-3% mỗi giao dịch

**→ Tổng chi phí ban đầu: ~2-7 triệu VNĐ**

---

**Good luck! 🚀**
