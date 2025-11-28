# 📚 HƯỚNG DẪN TÍCH HỢP VNPAY VÀO DỰ ÁN SPRING BOOT

## 📋 **MỤC LỤC**

1. [Giới thiệu](#1-giới-thiệu)
2. [Chuẩn bị](#2-chuẩn-bị)
3. [Tạo Database](#3-tạo-database)
4. [Tạo Domain Entity](#4-tạo-domain-entity)
5. [Tạo DAO Repository](#5-tạo-dao-repository)
6. [Tạo Configuration](#6-tạo-configuration)
7. [Tạo Service Layer](#7-tạo-service-layer)
8. [Tạo REST Controller](#8-tạo-rest-controller)
9. [Tạo View Controller](#9-tạo-view-controller)
10. [Tích hợp Frontend](#10-tích-hợp-frontend)
11. [Tạo View hiển thị kết quả](#11-tạo-view-hiển-thị-kết-quả)
12. [Testing](#12-testing)
13. [Troubleshooting](#13-troubleshooting)

---

## 1. GIỚI THIỆU

### 1.1. VNPay là gì?

VNPay là cổng thanh toán trực tuyến lớn nhất Việt Nam, cho phép:
- ✅ Thanh toán qua thẻ ATM nội địa
- ✅ Thanh toán qua thẻ Visa/Master/JCB
- ✅ Thanh toán qua QR Code
- ✅ Thanh toán qua ví điện tử

### 1.2. Flow thanh toán VNPay

```
┌─────────┐      ┌──────────┐      ┌─────────┐      ┌──────────┐
│  User   │      │  Website │      │  VNPay  │      │   Bank   │
└────┬────┘      └────┬─────┘      └────┬────┘      └────┬─────┘
     │                │                  │                 │
     │ 1. Chọn VNPay  │                  │                 │
     │───────────────>│                  │                 │
     │                │                  │                 │
     │                │ 2. Tạo Payment   │                 │
     │                │    & PaymentURL  │                 │
     │                │──────────────────>│                 │
     │                │                  │                 │
     │                │ 3. Return URL    │                 │
     │                │<──────────────────│                 │
     │                │                  │                 │
     │ 4. Redirect    │                  │                 │
     │<───────────────│                  │                 │
     │                │                  │                 │
     │ 5. Nhập thông tin thẻ            │                 │
     │──────────────────────────────────>│                 │
     │                │                  │                 │
     │                │                  │ 6. Xác thực     │
     │                │                  │────────────────>│
     │                │                  │                 │
     │                │                  │ 7. Kết quả      │
     │                │                  │<────────────────│
     │                │                  │                 │
     │                │ 8. Callback      │                 │
     │                │    (vnpay-return)│                 │
     │<───────────────────────────────────│                 │
     │                │                  │                 │
     │ 9. Hiển thị kết quả              │                 │
     │<───────────────│                  │                 │
     │                │                  │                 │
```

---

## 2. CHUẨN BỊ

### 2.1. Yêu cầu hệ thống

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- SQL Server Driver -->
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
    
    <!-- Thymeleaf (nếu dùng) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
</dependencies>
```

### 2.2. Đăng ký VNPay Sandbox

1. Truy cập: https://sandbox.vnpayment.vn/devreg/
2. Điền thông tin đăng ký
3. Xác nhận email
4. Nhận:
   - `Terminal ID` (vnp_TmnCode)
   - `Hash Secret` (vnp_HashSecret)

---

## 3. TẠO DATABASE

### 3.1. Tạo bảng Payments

```sql
-- File: payment_table.sql
CREATE TABLE Payments (
    paymentID INT PRIMARY KEY IDENTITY(1,1),
    orderID INT NOT NULL,
    paymentMethod NVARCHAR(20) NOT NULL,  -- 'VNPAY' hoặc 'COD'
    transactionID NVARCHAR(50),           -- Mã giao dịch từ VNPay
    amount BIGINT NOT NULL,               -- Số tiền (VNĐ)
    paymentStatus NVARCHAR(20) NOT NULL,  -- 'PENDING', 'SUCCESS', 'FAILED'
    paymentDate DATETIME DEFAULT GETDATE(),
    bankCode NVARCHAR(20),                -- Mã ngân hàng
    responseCode NVARCHAR(10),            -- Mã phản hồi từ VNPay
    description NVARCHAR(500),
    
    CONSTRAINT FK_Payments_Orders FOREIGN KEY (orderID) 
        REFERENCES Orders(orderID) ON DELETE CASCADE
);

-- Indexes để tăng performance
CREATE INDEX idx_payments_order ON Payments(orderID);
CREATE INDEX idx_payments_transaction ON Payments(transactionID);
CREATE INDEX idx_payments_status ON Payments(paymentStatus);
```

### 3.2. Chạy script

```bash
sqlcmd -S YOUR_SERVER -d YOUR_DATABASE -i payment_table.sql
```

---

## 4. TẠO DOMAIN ENTITY

### 4.1. Payment.java

```java
// File: src/main/java/module/Domain/Payment.java
package module.Domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Payments")
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentID;
    
    @ManyToOne
    @JoinColumn(name = "orderID")
    @JsonIgnoreProperties(value = {"payments", "orderDetails"})
    private Order order;
    
    @Column(nullable = false, length = 20)
    private String paymentMethod; // VNPAY hoặc COD
    
    @Column(length = 50)
    private String transactionID;
    
    @Column(nullable = false)
    private Long amount;
    
    @Column(nullable = false, length = 20)
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date paymentDate = new Date();
    
    @Column(length = 20)
    private String bankCode;
    
    @Column(length = 10)
    private String responseCode;
    
    @Column(length = 500)
    private String description;
    
    // Constructors
    public Payment() {
    }
    
    // Getters and Setters
    public Integer getPaymentID() {
        return paymentID;
    }
    
    public void setPaymentID(Integer paymentID) {
        this.paymentID = paymentID;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTransactionID() {
        return transactionID;
    }
    
    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
```

---

## 5. TẠO DAO REPOSITORY

### 5.1. PaymentDAO.java

```java
// File: src/main/java/module/DAO/PaymentDAO.java
package module.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import module.Domain.Payment;

public interface PaymentDAO extends JpaRepository<Payment, Integer> {
    
    /**
     * Tìm payment theo transaction ID
     */
    Optional<Payment> findByTransactionID(String transactionID);
    
    /**
     * Tìm tất cả payments của một order
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderID = :orderID")
    List<Payment> findByOrderID(@Param("orderID") Integer orderID);
    
    /**
     * Tìm payment theo orderID và status
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderID = :orderID AND p.paymentStatus = :status")
    List<Payment> findByOrderIDAndStatus(@Param("orderID") Integer orderID, 
                                         @Param("status") String status);
    
    /**
     * Đếm số payment thành công
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    Long countSuccessPayments();
}
```

---

## 6. TẠO CONFIGURATION

### 6.1. VNPayConfig.java

```java
// File: src/main/java/module/Config/VNPayConfig.java
package module.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration cho VNPay Payment Gateway
 */
@Configuration
public class VNPayConfig {
    
    @Value("${vnpay.url}")
    private String vnpayUrl;
    
    @Value("${vnpay.returnUrl}")
    private String returnUrl;
    
    @Value("${vnpay.tmnCode}")
    private String tmnCode;
    
    @Value("${vnpay.hashSecret}")
    private String hashSecret;
    
    @Value("${vnpay.apiUrl}")
    private String apiUrl;
    
    // Getters
    public String getVnpayUrl() {
        return vnpayUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public String getTmnCode() {
        return tmnCode;
    }
    
    public String getHashSecret() {
        return hashSecret;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
}
```

### 6.2. application.properties

```properties
# File: src/main/resources/application.properties

# VNPay Configuration - Sandbox
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay-return
vnpay.tmnCode=YOUR_TMN_CODE_HERE
vnpay.hashSecret=YOUR_HASH_SECRET_HERE
vnpay.apiUrl=https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
```

---

## 7. TẠO SERVICE LAYER

### 7.1. PaymentService.java (Interface)

```java
// File: src/main/java/module/Services/PaymentService.java
package module.Services;

import java.util.List;
import java.util.Map;

import module.Domain.Payment;

public interface PaymentService {
    
    /**
     * Tạo URL thanh toán VNPay
     */
    String createVNPayPaymentUrl(Integer orderID, Long amount, 
                                 String orderInfo, String ipAddress) throws Exception;
    
    /**
     * Xử lý callback từ VNPay
     */
    Payment processVNPayReturn(Map<String, String> params) throws Exception;
    
    /**
     * Tạo payment COD
     */
    Payment createCODPayment(Integer orderID, Long amount);
    
    /**
     * Lấy payment theo transaction ID
     */
    Payment getPaymentByTransactionID(String transactionID);
    
    /**
     * Kiểm tra order đã thanh toán chưa
     */
    boolean isOrderPaid(Integer orderID);
    
    /**
     * Lấy danh sách payments của order
     */
    List<Payment> getPaymentsByOrderID(Integer orderID);
    
    /**
     * Hủy payment
     */
    Payment cancelPayment(Integer paymentID) throws Exception;
}
```

### 7.2. PaymentServiceImpl.java

```java
// File: src/main/java/module/Services/Impl/PaymentServiceImpl.java
package module.Services.Impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import module.Config.VNPayConfig;
import module.DAO.OrderDAO;
import module.DAO.PaymentDAO;
import module.Domain.Order;
import module.Domain.Payment;
import module.Services.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentDAO paymentDAO;
    
    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private VNPayConfig vnPayConfig;
    
    @Override
    public String createVNPayPaymentUrl(Integer orderID, Long amount, 
                                        String orderInfo, String ipAddress) throws Exception {
        // 1. Kiểm tra order tồn tại
        Optional<Order> orderOpt = orderDAO.findById(orderID);
        if (!orderOpt.isPresent()) {
            throw new Exception("Order không tồn tại");
        }
        Order order = orderOpt.get();
        
        // 2. Tạo payment record với status PENDING
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("VNPAY");
        payment.setAmount(amount);
        payment.setPaymentStatus("PENDING");
        payment.setDescription(orderInfo);
        payment = paymentDAO.save(payment);
        
        // 3. Build VNPay params
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu x100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(payment.getPaymentID())); // Dùng paymentID
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        // Thời gian tạo và hết hạn
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // 4. Sắp xếp params và tạo query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        
        // 5. Tạo secure hash
        String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        
        // 6. Return payment URL
        String paymentUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl;
        return paymentUrl;
    }
    
    @Override
    public Payment processVNPayReturn(Map<String, String> params) throws Exception {
        // 1. Lấy secure hash từ VNPay gửi về
        String vnpSecureHash = params.get("vnp_SecureHash");
        
        // 2. Xóa các params không dùng để hash
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        // 3. Tạo lại hash để verify
        String signValue = hashAllFields(params);
        
        // 4. Verify signature
        if (!signValue.equals(vnpSecureHash)) {
            throw new Exception("Invalid signature");
        }
        
        // 5. Lấy thông tin từ params
        String txnRef = params.get("vnp_TxnRef"); // PaymentID
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");
        
        // 6. Tìm payment
        Optional<Payment> paymentOpt = paymentDAO.findById(Integer.parseInt(txnRef));
        if (!paymentOpt.isPresent()) {
            throw new Exception("Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        
        // 7. Update payment status
        payment.setTransactionID(transactionNo);
        payment.setResponseCode(responseCode);
        payment.setBankCode(bankCode);
        
        if ("00".equals(responseCode)) {
            payment.setPaymentStatus("SUCCESS");
            
            // Update order status
            Order order = payment.getOrder();
            order.setStatus(2); // Status = 2 (Đã thanh toán)
            orderDAO.save(order);
        } else {
            payment.setPaymentStatus("FAILED");
        }
        
        return paymentDAO.save(payment);
    }
    
    @Override
    public Payment createCODPayment(Integer orderID, Long amount) {
        Optional<Order> orderOpt = orderDAO.findById(orderID);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order không tồn tại");
        }
        
        Payment payment = new Payment();
        payment.setOrder(orderOpt.get());
        payment.setPaymentMethod("COD");
        payment.setAmount(amount);
        payment.setPaymentStatus("PENDING"); // Sẽ SUCCESS khi nhận hàng
        payment.setDescription("Thanh toán khi nhận hàng");
        
        return paymentDAO.save(payment);
    }
    
    @Override
    public Payment getPaymentByTransactionID(String transactionID) {
        return paymentDAO.findByTransactionID(transactionID).orElse(null);
    }
    
    @Override
    public boolean isOrderPaid(Integer orderID) {
        List<Payment> payments = paymentDAO.findByOrderIDAndStatus(orderID, "SUCCESS");
        return !payments.isEmpty();
    }
    
    @Override
    public List<Payment> getPaymentsByOrderID(Integer orderID) {
        return paymentDAO.findByOrderID(orderID);
    }
    
    @Override
    public Payment cancelPayment(Integer paymentID) throws Exception {
        Optional<Payment> paymentOpt = paymentDAO.findById(paymentID);
        if (!paymentOpt.isPresent()) {
            throw new Exception("Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            throw new Exception("Cannot cancel successful payment");
        }
        
        payment.setPaymentStatus("CANCELLED");
        return paymentDAO.save(payment);
    }
    
    // Helper methods
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        
        return hmacSHA512(vnPayConfig.getHashSecret(), sb.toString());
    }
}
```

---

## 8. TẠO REST CONTROLLER

### 8.1. PaymentRestController.java

```java
// File: src/main/java/module/RestController/PaymentRestController.java
package module.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import module.Domain.Payment;
import module.Services.PaymentService;

@CrossOrigin("*")
@RestController
@RequestMapping("/payment")
public class PaymentRestController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * API tạo URL thanh toán VNPay
     * POST /payment/create-vnpay-url
     * Body: { "orderID": 123, "amount": 15000000, "orderInfo": "..." }
     */
    @PostMapping("/create-vnpay-url")
    public ResponseEntity<?> createVNPayUrl(@RequestBody Map<String, Object> payload, 
                                           HttpServletRequest request) {
        try {
            Integer orderID = (Integer) payload.get("orderID");
            Long amount = Long.parseLong(payload.get("amount").toString());
            String orderInfo = (String) payload.get("orderInfo");
            
            String ipAddress = getIpAddress(request);
            String paymentUrl = paymentService.createVNPayPaymentUrl(orderID, amount, orderInfo, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("message", "Tạo URL thanh toán thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * API tạo payment COD
     * POST /payment/create-cod
     * Body: { "orderID": 123, "amount": 15000000 }
     */
    @PostMapping("/create-cod")
    public ResponseEntity<?> createCODPayment(@RequestBody Map<String, Object> payload) {
        try {
            Integer orderID = (Integer) payload.get("orderID");
            Long amount = Long.parseLong(payload.get("amount").toString());
            
            Payment payment = paymentService.createCODPayment(orderID, amount);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    /**
     * API kiểm tra order đã thanh toán chưa
     * GET /payment/check-paid/{orderID}
     */
    @GetMapping("/check-paid/{orderID}")
    public ResponseEntity<?> checkOrderPaid(@PathVariable Integer orderID) {
        boolean isPaid = paymentService.isOrderPaid(orderID);
        Map<String, Object> response = new HashMap<>();
        response.put("orderID", orderID);
        response.put("isPaid", isPaid);
        return ResponseEntity.ok(response);
    }
    
    /**
     * API lấy payments của order
     * GET /payment/by-order/{orderID}
     */
    @GetMapping("/by-order/{orderID}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Integer orderID) {
        List<Payment> payments = paymentService.getPaymentsByOrderID(orderID);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * API hủy payment
     * PUT /payment/cancel/{paymentID}
     */
    @PutMapping("/cancel/{paymentID}")
    public ResponseEntity<?> cancelPayment(@PathVariable Integer paymentID) {
        try {
            Payment payment = paymentService.cancelPayment(paymentID);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // Helper method
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
```

---

## 9. TẠO VIEW CONTROLLER

### 9.1. PaymentController.java

```java
// File: src/main/java/module/Controller/PaymentController.java
package module.Controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import module.Domain.Payment;
import module.Services.PaymentService;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Xử lý callback từ VNPay
     * GET /payment/vnpay-return?vnp_ResponseCode=00&vnp_TransactionNo=...
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        try {
            // Process VNPay return
            Payment payment = paymentService.processVNPayReturn(params);
            
            // Add data to model
            model.addAttribute("payment", payment);
            model.addAttribute("success", "00".equals(payment.getResponseCode()));
            model.addAttribute("message", getMessageFromResponseCode(payment.getResponseCode()));
            
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        
        return "payment-result"; // Trả về view payment-result.html
    }
    
    // Helper method
    private String getMessageFromResponseCode(String responseCode) {
        Map<String, String> messages = new HashMap<>();
        messages.put("00", "Giao dịch thành công");
        messages.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ");
        messages.put("09", "Thẻ/Tài khoản chưa đăng ký dịch vụ");
        messages.put("10", "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        messages.put("11", "Đã hết hạn chờ thanh toán");
        messages.put("12", "Thẻ/Tài khoản bị khóa");
        messages.put("24", "Người dùng hủy giao dịch");
        messages.put("51", "Tài khoản không đủ số dư");
        messages.put("65", "Vượt quá hạn mức thanh toán trong ngày");
        
        return messages.getOrDefault(responseCode, "Giao dịch không thành công");
    }
}
```

---

## 10. TÍCH HỢP FRONTEND

### 10.1. Thêm modal chọn phương thức thanh toán

```javascript
// File: src/main/resources/static/User/userJS/order.js

// Thêm vào controller AngularJS
$scope.showPaymentMethodModal = function(){
    Swal.fire({
        title: 'Chọn phương thức thanh toán',
        html: `
            <div style="display: flex; flex-direction: column; gap: 15px;">
                <button id="btnCOD" class="swal2-confirm swal2-styled" 
                        style="background-color: #6c757d;">
                    <i class="fa fa-money"></i> Thanh toán khi nhận hàng (COD)
                </button>
                <button id="btnVNPay" class="swal2-confirm swal2-styled" 
                        style="background-color: #0088cc;">
                    <i class="fa fa-credit-card"></i> Thanh toán VNPay
                </button>
            </div>
        `,
        showConfirmButton: false,
        showCancelButton: true,
        cancelButtonText: 'Hủy',
        didOpen: () => {
            document.getElementById('btnCOD').addEventListener('click', () => {
                Swal.close();
                $scope.paymentMethod = 'COD';
                $scope.processOrder();
            });
            document.getElementById('btnVNPay').addEventListener('click', () => {
                Swal.close();
                $scope.paymentMethod = 'VNPAY';
                $scope.processOrder();
            });
        }
    });
};
```

### 10.2. Xử lý order và payment

```javascript
$scope.processOrder = function(){
    var user = $("#usernameCart").text();
    
    // Lấy cart và tạo order (code hiện có)
    $http.get(`http://localhost:8080/CartItem/cartItems/${user}`).then(resitem => {
        $scope.itemcart = resitem.data;
        
        // ... Code tạo order ...
        
        $http.post(`http://localhost:8080/order/Order/`, dataOder).then(resOder => {
            
            // Xử lý payment theo phương thức đã chọn
            if($scope.paymentMethod === 'VNPAY'){
                // Tạo payment VNPay
                var paymentData = {
                    orderID: resOder.data.orderID,
                    amount: totalAmount,
                    orderInfo: 'Thanh toan don hang #' + resOder.data.orderID
                };
                
                $http.post(`http://localhost:8080/payment/create-vnpay-url`, paymentData)
                .then(respPayment => {
                    if(respPayment.data.success){
                        // Redirect đến VNPay
                        window.location.href = respPayment.data.paymentUrl;
                    } else {
                        Swal.fire('Lỗi', respPayment.data.message, 'error');
                    }
                }).catch(error => {
                    Swal.fire('Lỗi', 'Không thể tạo link thanh toán VNPay', 'error');
                });
                
            } else {
                // COD
                var codPaymentData = {
                    orderID: resOder.data.orderID,
                    amount: totalAmount
                };
                
                $http.post(`http://localhost:8080/payment/create-cod`, codPaymentData)
                .then(respCOD => {
                    Swal.fire({
                        title: 'Đặt Hàng Thành Công!',
                        html: `
                            <p>Mã đơn hàng: #${resOder.data.orderID}</p>
                            <p>Tổng tiền: ${totalAmount.toLocaleString()} VNĐ</p>
                            <p>Thanh toán: Khi nhận hàng (COD)</p>
                        `,
                        icon: 'success'
                    }).then(() => {
                        window.location.href = '/order';
                    });
                });
            }
        });
    });
};
```

### 10.3. Update button Order trong cart.html

```html
<!-- File: src/main/resources/templates/Usersform/cart.html -->
<div class="wc-proceed-to-checkout" ng-if="getTotal() > 0">
    <a ng-click="addOrder()">Order</a>
</div>
```

---

## 11. TẠO VIEW HIỂN THỊ KẾT QUẢ

### 11.1. payment-result.html

```html
<!-- File: src/main/resources/templates/payment-result.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Kết quả thanh toán</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        .payment-result {
            max-width: 600px;
            margin: 100px auto;
            padding: 30px;
            text-align: center;
        }
        .success-icon {
            font-size: 80px;
            color: #28a745;
        }
        .failed-icon {
            font-size: 80px;
            color: #dc3545;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="payment-result card shadow">
            <!-- Success -->
            <div th:if="${success}">
                <i class="fas fa-check-circle success-icon"></i>
                <h2 class="mt-3">Thanh toán thành công!</h2>
                <p th:text="${message}"></p>
                
                <div class="mt-4">
                    <p><strong>Mã đơn hàng:</strong> <span th:text="${payment.order.orderID}"></span></p>
                    <p><strong>Mã giao dịch:</strong> <span th:text="${payment.transactionID}"></span></p>
                    <p><strong>Số tiền:</strong> <span th:text="${#numbers.formatDecimal(payment.amount, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"></span></p>
                    <p><strong>Ngân hàng:</strong> <span th:text="${payment.bankCode}"></span></p>
                </div>
                
                <div class="mt-4">
                    <a href="/order" class="btn btn-primary">Xem đơn hàng</a>
                    <a href="/" class="btn btn-secondary">Về trang chủ</a>
                </div>
            </div>
            
            <!-- Failed -->
            <div th:unless="${success}">
                <i class="fas fa-times-circle failed-icon"></i>
                <h2 class="mt-3">Thanh toán thất bại!</h2>
                <p th:text="${message}"></p>
                
                <div class="mt-4">
                    <a href="/cart" class="btn btn-primary">Thử lại</a>
                    <a href="/" class="btn btn-secondary">Về trang chủ</a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

---

## 12. TESTING

### 12.1. Test COD

1. Thêm sản phẩm vào giỏ hàng
2. Bấm Order
3. Chọn "Thanh toán khi nhận hàng (COD)"
4. Kiểm tra:
   - ✅ Order được tạo
   - ✅ Payment được tạo với status PENDING
   - ✅ Hiển thị thông báo thành công

### 12.2. Test VNPay

1. Đăng ký VNPay Sandbox: https://sandbox.vnpayment.vn/devreg/
2. Cập nhật `tmnCode` và `hashSecret` trong application.properties
3. Restart server
4. Thêm sản phẩm vào giỏ
5. Bấm Order → Chọn "Thanh toán VNPay"
6. Chọn ngân hàng test (NCB)
7. Nhập thông tin thẻ test:
   ```
   Số thẻ: 9704198526191432198
   Tên: NGUYEN VAN A
   Ngày: 07/15
   OTP: 123456
   ```
8. Kiểm tra:
   - ✅ Redirect về trang kết quả
   - ✅ Payment status = SUCCESS
   - ✅ Order status = 2 (Đã thanh toán)

### 12.3. Kiểm tra Database

```sql
-- Xem tất cả payments
SELECT * FROM Payments ORDER BY paymentDate DESC;

-- Xem payments của order cụ thể
SELECT * FROM Payments WHERE orderID = 123;

-- Xem payments thành công
SELECT * FROM Payments WHERE paymentStatus = 'SUCCESS';
```

---

## 13. TROUBLESHOOTING

### 13.1. Lỗi "Không tìm thấy website" (Code 72)

**Nguyên nhân:**
- `vnp_TmnCode` không đúng
- Chưa đăng ký VNPay Sandbox

**Giải pháp:**
- Đăng ký tại: https://sandbox.vnpayment.vn/devreg/
- Cập nhật `tmnCode` trong application.properties
- Restart server

### 13.2. Lỗi signature không hợp lệ (Code 97)

**Nguyên nhân:**
- `vnp_HashSecret` không đúng
- Encoding không đúng UTF-8
- Thứ tự params sai khi hash

**Giải pháp:**
- Check `hashSecret` trong application.properties
- Đảm bảo encoding UTF-8
- Đảm bảo params được sort trước khi hash

### 13.3. Callback không về

**Nguyên nhân:**
- `vnp_ReturnUrl` không accessible
- Server không chạy
- Firewall chặn

**Giải pháp:**
- Đảm bảo server đang chạy
- Check `returnUrl` trong application.properties
- Dùng ngrok nếu test trên localhost

### 13.4. Payment không lưu vào DB

**Nguyên nhân:**
- Foreign key constraint (orderID không tồn tại)
- Database connection lỗi

**Giải pháp:**
- Check order đã tồn tại chưa
- Check database connection
- Xem server logs

---

## 📚 **TÀI LIỆU THAM KHẢO**

- VNPay API Documentation: https://sandbox.vnpayment.vn/apis/docs/
- VNPay Error Codes: https://sandbox.vnpayment.vn/apis/docs/bang-ma-loi/
- Spring Boot Documentation: https://spring.io/projects/spring-boot

---

## ✅ **CHECKLIST HOÀN THÀNH**

```
☐ Tạo database table Payments
☐ Tạo Payment entity
☐ Tạo PaymentDAO
☐ Tạo VNPayConfig
☐ Tạo PaymentService interface
☐ Implement PaymentServiceImpl
☐ Tạo PaymentRestController
☐ Tạo PaymentController
☐ Update frontend (order.js)
☐ Tạo payment-result.html
☐ Update application.properties
☐ Test COD
☐ Test VNPay
☐ Kiểm tra database
```

---

**🎉 CHÚC MỪNG! Bạn đã tích hợp thành công VNPay!**
