# HƯỚNG DẪN VIẾT BÁO CÁO ĐỒ ÁN JAVA 6

## MỤC LỤC BÁO CÁO

### PHẦN 1: XÂY DỰNG VÀ PHÁT TRIỂN DASHBOARD (6 ĐIỂM)
**Cấu trúc điểm: 2-1-1-2** (Spring Boot)

---

## I. XÂY DỰNG HOÀN CHỈNH HỆ THỐNG DASHBOARD (2 ĐIỂM)

### 1.1. Giới thiệu Dashboard Admin
**Nội dung cần viết:**
- Dashboard là hệ thống quản trị tập trung cho Admin
- Quản lý: Sản phẩm, Danh mục, Thương hiệu, Đơn hàng, Tài khoản
- Công nghệ: Spring Boot 2.5.2, AngularJS, Bootstrap Admin Template

**Screenshot cần chụp:**
- Giao diện tổng quan Dashboard (`http://localhost:8080/admin/index`)
- Menu sidebar với các chức năng

**Code minh họa:**
```java
// File: IndexController.java
@Controller
public class IndexController {
    @GetMapping("/admin/index")
    public String admin() {
        return "Adminform/pages/index.html";
    }
}
```

### 1.2. Quản lý Sản phẩm (Products)
**Nội dung:**
- CRUD đầy đủ: Thêm, Sửa, Xóa, Tìm kiếm
- Upload ảnh lên Firebase Storage
- Phân trang với AngularJS
- REST API backend

**Screenshot:**
- Trang danh sách sản phẩm (`/Adminform/pages/product.html`)
- Modal thêm/sửa sản phẩm
- Giao diện upload ảnh

**Code backend:**
```java
// File: ProductRestController.java
@RestController
@RequestMapping("/products")
public class ProductRestController {
    @GetMapping
    public List<Products> getAll() {
        return productDAO.findAll();
    }
    
    @PostMapping
    public Products create(@RequestBody Products product) {
        return productDAO.save(product);
    }
}
```

**Code frontend:**
```javascript
// File: product.js
$scope.create = function() {
    $http.post('/products', $scope.form)
        .then(resp => {
            Swal.fire('Success', 'Thêm sản phẩm thành công!', 'success');
            $scope.load();
        });
};
```

### 1.3. Quản lý Danh mục & Thương hiệu
**Nội dung:**
- Category (Laptop, PC, Linh kiện, Phụ kiện)
- Brand (Dell, HP, Asus, Acer, MSI, etc.)
- CRUD đầy đủ với validation

**Screenshot:**
- Trang Category
- Trang Brand

### 1.4. Quản lý Đơn hàng (Order Management)
**Nội dung:**
- Xem danh sách đơn hàng
- Xác nhận đơn (status: 0→1→2→4)
- Hủy đơn (status: 3)
- Gửi email thông báo cho khách hàng
- Xem chi tiết sản phẩm trong đơn

**Screenshot:**
- Tab "Order confirmation" với danh sách đơn chờ xác nhận
- Modal "Info Product" hiển thị chi tiết

**Code xác nhận đơn:**
```javascript
// File: statiscal.js
$scope.xacnhan = function(orderid) {
    $http.get(`/statistical/confirm/${orderid}`)
        .then(resp => {
            var item = resp.data;
            item.status = 1; // Xác nhận
            return $http.put(`/statistical/confirm/${orderid}`, item);
        })
        .then(resp => {
            Swal.fire('Success', 'Xác nhận đơn hàng thành công!', 'success');
            // Gửi email
            $http.post('/send/orders', item);
            $scope.load();
        });
};
```

### 1.5. Quản lý Tài khoản
**Nội dung:**
- Xem danh sách user
- Phân quyền (Role: USER, ADMIN)
- Spring Security authentication

**Screenshot:**
- Trang Accounts
- Bảng phân quyền

---

## II. BIỂU ĐỒ HÓA, TRỰC QUAN HÓA SỐ LIỆU (1 ĐIỂM)

### 2.1. Biểu đồ Doanh thu (Turnover Chart)
**Nội dung:**
- Thống kê theo Ngày/Tháng/Năm
- Biểu đồ đường (Line Chart) hiển thị:
  - Trục Y1: Doanh thu (VND)
  - Trục Y2: Số lượng đơn hàng
- Công nghệ: Chart.js

**Screenshot:**
- Tab "Turnover" với 3 nút (Theo Ngày/Tháng/Năm)
- Biểu đồ Line Chart đầy màu sắc
- Bảng dữ liệu dưới biểu đồ

**SQL Query:**
```sql
-- Thống kê theo ngày
SELECT order_date, COUNT(orderid) as 'count', SUM(amount) as 'sum' 
FROM orders 
WHERE status = 4
GROUP BY order_date
ORDER BY order_date DESC
```

**Code frontend:**
```javascript
// File: statiscal.js
$scope.drawTurnoverChart = function(data) {
    const ctx = document.getElementById('turnoverChart').getContext('2d');
    const labels = data.map(item => item[0]); // Ngày/Tháng/Năm
    const turnoverData = data.map(item => item[2]); // Doanh thu
    const quantityData = data.map(item => item[1]); // Số đơn
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Doanh thu (VND)',
                    data: turnoverData,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    yAxisID: 'y'
                },
                {
                    label: 'Số lượng đơn hàng',
                    data: quantityData,
                    borderColor: 'rgba(255, 99, 132, 1)',
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            scales: {
                y: { position: 'left' },
                y1: { position: 'right' }
            }
        }
    });
};
```

### 2.2. Top 5 Sản phẩm bán chạy
**Nội dung:**
- Biểu đồ cột (Bar Chart)
- Hiển thị tỉ lệ % so với tổng sản phẩm đã bán
- Bảng dữ liệu: TOP, Tên, Tổng tiền, Số lượng

**Screenshot:**
- Tab "Top 5 sản phẩm bán chạy"
- Bar Chart với màu sắc
- Bảng TOP 1→5

**SQL Query:**
```sql
SELECT TOP 5 p.name, SUM(od.quantity * od.unit_price) as total, 
       SUM(od.quantity) as quantity
FROM order_detail od 
JOIN products p ON od.productid = p.productid
JOIN orders o ON od.orderid = o.orderid
WHERE o.status = 4
GROUP BY p.name
ORDER BY total DESC
```

**Code Chart:**
```javascript
$scope.drawBarChart = function(topItems) {
    const labels = topItems.map(item => item[0]);
    const salesData = topItems.map(item => item[2]);
    const totalSales = allSalesData.reduce((sum, cur) => sum + cur, 0);
    const percentageData = salesData.map(sale => 
        ((sale / totalSales) * 100).toFixed(2)
    );
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Tỉ lệ % sản phẩm đã bán',
                data: percentageData,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(255, 159, 64, 0.2)',
                    'rgba(255, 205, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(54, 162, 235, 0.2)'
                ]
            }]
        }
    });
};
```

### 2.3. Top 5 Khách hàng mua nhiều nhất
**Nội dung:**
- Biểu đồ đường (Line Chart)
- Hiển thị: Tài khoản, Tên, Số đơn hàng, Tổng tiền

**Screenshot:**
- Tab "Top 5 Khách hàng"
- Line Chart
- Bảng TOP 1→5

**SQL Query:**
```sql
SELECT TOP 5 a.email, a.name, COUNT(o.orderid) as orders_count,
       SUM(o.amount) as total_amount
FROM accounts a
JOIN orders o ON a.email = o.user_id
WHERE o.status = 4
GROUP BY a.email, a.name
ORDER BY orders_count DESC
```

---

## III. TÍNH NĂNG ĐỘT PHÁ VỀ MẶT KỸ THUẬT (1 ĐIỂM)

### 3.1. Tích hợp VNPay Payment Gateway
**Nội dung:**
- Thanh toán online qua VNPay Sandbox
- HMAC SHA512 signature verification
- Xử lý callback và cập nhật trạng thái đơn hàng
- **ĐÂY LÀ TÍNH NĂNG NỔI BẬT NHẤT!**

**Luồng hoạt động:**
1. User chọn "Thanh toán Online"
2. Tạo order với status = 0 (PENDING)
3. Redirect đến VNPay với URL đã mã hóa
4. User thanh toán tại VNPay
5. VNPay callback về `/payment/vnpay-return`
6. Verify signature, cập nhật status = 1 (CONFIRMED)
7. Xóa giỏ hàng, gửi email xác nhận

**Screenshot:**
- SweetAlert2 modal chọn phương thức thanh toán
- Trang VNPay Sandbox
- Trang kết quả thanh toán
- Email xác nhận

**Code backend:**
```java
// File: PaymentServiceImpl.java
@Override
public String createVNPayPaymentUrl(Long orderID, Long amount, String ipAddress) {
    Map<String, String> vnpParams = new TreeMap<>();
    vnpParams.put("vnp_Version", "2.1.0");
    vnpParams.put("vnp_Command", "pay");
    vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
    vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
    vnpParams.put("vnp_CurrCode", "VND");
    vnpParams.put("vnp_TxnRef", String.valueOf(orderID));
    vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + orderID);
    vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
    vnpParams.put("vnp_IpAddr", ipAddress);
    // ... thêm CreateDate, ExpireDate, Locale
    
    // Build hashData (không encode fieldName, có encode fieldValue)
    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();
    for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
        hashData.append(entry.getKey())
                .append('=')
                .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
             .append('=')
             .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        hashData.append('&');
        query.append('&');
    }
    
    String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
    String paymentUrl = vnPayConfig.getUrl() + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;
    
    return paymentUrl;
}

@Override
public Map<String, String> processVNPayReturn(Map<String, String> params) {
    String vnpSecureHash = params.get("vnp_SecureHash");
    params.remove("vnp_SecureHash");
    
    String calculatedHash = hashAllFields(params);
    
    if (vnpSecureHash.equals(calculatedHash)) {
        if ("00".equals(params.get("vnp_ResponseCode"))) {
            // Thanh toán thành công
            Long orderID = Long.parseLong(params.get("vnp_TxnRef"));
            Order order = orderDAO.findById(orderID).orElse(null);
            order.setStatus(1); // CONFIRMED
            orderDAO.save(order);
            
            // Lưu payment
            Payment payment = new Payment();
            payment.setOrderID(orderID);
            payment.setPaymentMethod("VNPAY");
            payment.setTransactionID(params.get("vnp_TransactionNo"));
            payment.setAmount(Long.parseLong(params.get("vnp_Amount")) / 100);
            payment.setPaymentStatus("SUCCESS");
            paymentDAO.save(payment);
            
            return Map.of("status", "success");
        }
    }
    return Map.of("status", "failed");
}
```

**Code frontend:**
```javascript
// File: order.js
$scope.addOrder = function() {
    if ($scope.paymentMethod === 'VNPAY') {
        // Tạo order với status = 0
        var order = {
            adress: $scope.adress,
            phone: $scope.phone,
            status: 0, // PENDING
            accountoder: { email: username }
        };
        
        $http.post('/orders', order)
            .then(resp => {
                var orderID = resp.data.orderID;
                var totalAmount = $scope.totalamount();
                
                // Tạo VNPay payment URL
                return $http.post('/payment/create-vnpay-url', {
                    orderID: orderID,
                    amount: totalAmount
                });
            })
            .then(resp => {
                // Lưu cartDetailIDs để xóa sau khi thanh toán
                var cartDetailIDs = $scope.cartDetail.map(item => item.cartDetailid);
                sessionStorage.setItem('pendingCartItems', JSON.stringify(cartDetailIDs));
                
                // Redirect đến VNPay
                window.location.href = resp.data.paymentUrl;
            });
    } else {
        // COD: tạo order với status = 1, xóa cart ngay
        var order = {
            adress: $scope.adress,
            phone: $scope.phone,
            status: 1, // CONFIRMED
            accountoder: { email: username }
        };
        // ... tạo order, xóa cart
    }
};
```

**File cấu hình:**
```properties
# application.properties
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay-return
vnpay.tmnCode=LBJ0EVHH
vnpay.hashSecret=WN0OP4P4C23BBD98ERF14NTNK0GEYT6L
```

### 3.2. Upload ảnh lên Firebase Storage
**Nội dung:**
- Tích hợp Firebase Storage
- Upload ảnh sản phẩm từ form Admin
- Lưu URL vào database

**Code:**
```javascript
// Firebase upload logic
firebase.storage().ref('products/' + filename).put(file)
    .then(snapshot => snapshot.ref.getDownloadURL())
    .then(url => {
        $scope.form.image = url;
    });
```

### 3.3. Gửi Email tự động
**Nội dung:**
- JavaMailSender với Gmail SMTP
- Gửi email khi:
  - Đăng ký tài khoản thành công
  - Xác nhận đơn hàng
  - Hủy đơn hàng
  - Thanh toán VNPay thành công

**Code:**
```java
// File: MailServiceImpl.java
@Override
public void send(MailInfo mail) {
    MimeMessage message = sender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
    helper.setFrom(mail.getFrom());
    helper.setTo(mail.getTo());
    helper.setSubject(mail.getSubject());
    helper.setText(mail.getBody(), true); // HTML
    
    sender.send(message);
}
```

### 3.4. REST API với Spring Boot
**Nội dung:**
- RESTful API cho tất cả modules
- @CrossOrigin("*") cho phép CORS
- ResponseEntity<> với HTTP status codes

**Ví dụ:**
```java
@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ProductRestController {
    @GetMapping("/products")
    public ResponseEntity<List<Products>> getAll() {
        return ResponseEntity.ok(productDAO.findAll());
    }
}
```

---

## IV. KIỂM THỬ (2 ĐIỂM)

### 4.1. Kiểm thử chức năng (Functional Testing)
**Nội dung:**
- Test CRUD sản phẩm
- Test thêm vào giỏ hàng
- Test thanh toán COD
- Test thanh toán VNPay
- Test xác nhận đơn hàng
- Test gửi email

**Bảng test case:**
| Test Case ID | Mô tả | Input | Expected Output | Actual Output | Status |
|--------------|-------|-------|-----------------|---------------|--------|
| TC_PRD_01 | Thêm sản phẩm | Name, Price, Image | Sản phẩm mới xuất hiện | ✓ | PASS |
| TC_CART_01 | Thêm vào giỏ | ProductID, Quantity | Số lượng tăng | ✓ | PASS |
| TC_PAY_01 | Thanh toán COD | Địa chỉ, SDT | Order status=1 | ✓ | PASS |
| TC_PAY_02 | Thanh toán VNPay | Thẻ test | Redirect VNPay | ✓ | PASS |
| TC_ORD_01 | Xác nhận đơn | OrderID | Status 0→1 | ✓ | PASS |

**Screenshot:**
- Form thêm sản phẩm → Kết quả
- Giỏ hàng trước và sau khi thêm
- Trang thanh toán VNPay
- Email xác nhận

### 4.2. Kiểm thử hiệu năng (Performance Testing)
**Nội dung:**
- Test tốc độ load trang Dashboard
- Test database query performance
- Test API response time

**Công cụ:** Chrome DevTools, Postman

**Kết quả:**
- Dashboard load: < 2s
- API response: < 500ms
- Database query: < 100ms

**Screenshot:**
- Chrome DevTools Network tab
- Postman response time

### 4.3. Kiểm thử tương thích giao diện (UI/UX Testing)
**Nội dung:**
- Test trên các trình duyệt: Chrome, Firefox, Edge
- Test responsive design (Desktop, Tablet, Mobile)
- Test các độ phân giải khác nhau

**Screenshot:**
- Dashboard trên Chrome vs Firefox
- Mobile view của trang User
- Tablet view

---

## PHẦN 2: XÂY DỰNG VÀ PHÁT TRIỂN PHÍA BROWSER (4 ĐIỂM)

## I. LẬP TRÌNH HOÀN THIỆN PHÍA NGƯỜI DÙNG

### 1.1. Trang chủ (Home)
**Nội dung:**
- Slider banner
- Sản phẩm nổi bật
- Danh mục sản phẩm
- Template: Bootstrap E-commerce

**Screenshot:**
- Hero banner
- Grid sản phẩm
- Footer

**Code:**
```html
<!-- home.html -->
<div class="slider-area">
    <div class="slider-active owl-carousel">
        <div class="single-slider" ng-repeat="banner in banners">
            <img ng-src="{{banner.image}}" alt="Banner">
        </div>
    </div>
</div>

<div class="product-area">
    <div class="container">
        <div class="row">
            <div class="col-md-3" ng-repeat="product in products">
                <div class="product-item">
                    <img ng-src="{{product.image}}">
                    <h4>{{product.name}}</h4>
                    <p>{{product.unit_price | currency:"":0}} VND</p>
                    <button ng-click="addToCart(product)">Add to Cart</button>
                </div>
            </div>
        </div>
    </div>
</div>
```

### 1.2. Danh sách sản phẩm (Shop)
**Nội dung:**
- Lọc theo danh mục, thương hiệu
- Sắp xếp theo giá, tên
- Tìm kiếm
- Phân trang

**Screenshot:**
- Sidebar filter
- Grid sản phẩm
- Pagination

### 1.3. Chi tiết sản phẩm (Product Detail)
**Nội dung:**
- Ảnh sản phẩm lớn
- Thông tin chi tiết: Tên, Giá, Mô tả, Số lượng
- Nút "Add to Cart"
- Sản phẩm liên quan

**Screenshot:**
- Product detail page

### 1.4. Giỏ hàng (Shopping Cart)
**Nội dung:**
- Hiển thị danh sách sản phẩm trong giỏ
- Tăng/giảm số lượng
- Xóa sản phẩm
- Tính tổng tiền
- Nút "Checkout"

**Screenshot:**
- Cart table
- Tổng tiền

**Code:**
```javascript
// cart.js
$scope.increaseQuantity = function(item) {
    item.quantity++;
    $scope.updateCartItem(item);
};

$scope.decreaseQuantity = function(item) {
    if (item.quantity > 1) {
        item.quantity--;
        $scope.updateCartItem(item);
    }
};

$scope.removeItem = function(cartDetailID) {
    $http.delete('/cartItemDetail/' + cartDetailID)
        .then(() => {
            Swal.fire('Success', 'Đã xóa sản phẩm!', 'success');
            $scope.loadCart();
        });
};

$scope.totalAmount = function() {
    return $scope.cartItems.reduce((sum, item) => 
        sum + (item.productitem.unit_price * item.quantity), 0
    );
};
```

### 1.5. Thanh toán (Checkout)
**Nội dung:**
- Form nhập thông tin: Địa chỉ, Số điện thoại
- Chọn phương thức thanh toán:
  - COD (Thanh toán khi nhận hàng)
  - VNPay (Thanh toán online)
- SweetAlert2 modal hiển thị lựa chọn
- Validation form

**Screenshot:**
- Checkout form
- SweetAlert2 modal với 2 nút

**Code:**
```javascript
// order.js
$scope.checkout = function() {
    if (!$scope.adress || !$scope.phone) {
        Swal.fire('Error', 'Vui lòng nhập đầy đủ thông tin!', 'error');
        return;
    }
    
    Swal.fire({
        title: 'Chọn phương thức thanh toán',
        showDenyButton: true,
        confirmButtonText: 'Thanh toán Online',
        denyButtonText: 'Thanh toán khi nhận hàng',
        customClass: {
            confirmButton: 'swal-vnpay',
            denyButton: 'swal-cod'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            $scope.paymentMethod = 'VNPAY';
        } else if (result.isDenied) {
            $scope.paymentMethod = 'COD';
        }
        $scope.addOrder();
    });
};
```

### 1.6. Quản lý đơn hàng (My Orders)
**Nội dung:**
- Tab "Đơn hàng đang mua" (status: 0, 1, 2)
- Tab "Đơn hàng đã hoàn thành" (status: 4)
- Xem chi tiết từng đơn

**Screenshot:**
- Order list
- Order detail

### 1.7. Đăng nhập / Đăng ký
**Nội dung:**
- Form login với Spring Security
- Form register
- Validation
- Gửi email chào mừng

**Screenshot:**
- Login page
- Register page

---

## II. TÍNH NĂNG NỔI BẬT PHÍA BROWSER

### 2.1. Thanh toán VNPay (Đã mô tả ở phần Dashboard)

### 2.2. Ajax không reload trang
**Nội dung:**
- Thêm vào giỏ hàng không reload
- Cập nhật số lượng realtime
- SweetAlert2 notification

**Code:**
```javascript
$scope.addToCart = function(product) {
    var cartItem = {
        productitem: product,
        quantity: 1
    };
    
    $http.post('/cartItemDetail', cartItem)
        .then(() => {
            Swal.fire({
                icon: 'success',
                title: 'Đã thêm vào giỏ!',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 2000
            });
            $scope.updateCartCount();
        });
};
```

### 2.3. Tìm kiếm sản phẩm
**Nội dung:**
- Search box trên header
- Tìm theo tên sản phẩm
- Hiển thị kết quả realtime

### 2.4. Filter & Sort
**Nội dung:**
- Lọc theo giá: < 5tr, 5-10tr, 10-20tr, > 20tr
- Lọc theo brand, category
- Sắp xếp: Giá tăng/giảm, Tên A-Z

---

## III. KIỂM THỬ GIAO DIỆN TOÀN HỆ THỐNG

### 3.1. Kiểm thử chức năng User
**Test cases:**
- Đăng nhập/Đăng ký
- Xem sản phẩm
- Thêm vào giỏ
- Thanh toán COD/VNPay
- Xem đơn hàng

### 3.2. Kiểm thử responsive
**Screenshots:**
- Desktop (1920x1080)
- Tablet (768x1024)
- Mobile (375x667)

### 3.3. Kiểm thử trình duyệt
**Screenshots:**
- Chrome
- Firefox
- Edge

---

## PHẦN 3: YÊU CẦU CHUNG

### I. CẤU TRÚC DỰ ÁN

#### 1. Cấu trúc thư mục
```
AsignmentJava6/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── module/
│   │   │       ├── Config/
│   │   │       │   ├── AuthConfig.java
│   │   │       │   ├── SwaggerConfig.java
│   │   │       │   └── VNPayConfig.java
│   │   │       ├── Controller/
│   │   │       │   ├── IndexController.java
│   │   │       │   └── ProductController.java
│   │   │       ├── RestController/
│   │   │       │   ├── ProductRestController.java
│   │   │       │   ├── OrderRestController.java
│   │   │       │   └── PaymentRestController.java
│   │   │       ├── DAO/
│   │   │       │   ├── ProductDAO.java
│   │   │       │   └── OrderDAO.java
│   │   │       ├── Domain/
│   │   │       │   ├── Products.java
│   │   │       │   ├── Order.java
│   │   │       │   └── Payment.java
│   │   │       └── Services/
│   │   │           └── Impl/
│   │   │               ├── PaymentServiceImpl.java
│   │   │               └── MailServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── Admin/
│   │       │   │   ├── css/
│   │       │   │   ├── js/
│   │       │   │   └── MainJs/
│   │       │   │       ├── product.js
│   │       │   │       ├── statiscal.js
│   │       │   │       └── order.js
│   │       │   └── User/
│   │       │       ├── css/
│   │       │       └── js/
│   │       └── templates/
│   │           ├── Adminform/
│   │           │   └── pages/
│   │           │       ├── index.html
│   │           │       ├── product.html
│   │           │       └── statistical.html
│   │           └── Usersform/
│   │               ├── home.html
│   │               ├── cart.html
│   │               └── checkout.html
│   └── test/
│       └── java/
└── pom.xml
```

#### 2. Database Schema
**Screenshot:** ERD diagram từ SQL Server Management Studio

**Các bảng chính:**
- accounts (email, name, password, register_date)
- account_roles (email, role_id)
- roles (role_id, name)
- products (productid, name, unit_price, image, description, categoryid, brandid)
- categories (categoryid, name)
- brand (brandid, name)
- cart_items (cartid, username)
- cart_item_details (cart_detailid, cartid, productid, quantity)
- orders (orderid, user_id, adress, phone, amount, order_date, status)
- order_detail (order_detailid, orderid, productid, quantity, unit_price)
- payments (payment_id, order_id, payment_method, transaction_id, amount, payment_status, payment_date)

**SQL Script:**
```sql
-- File: tmartshop.sql
CREATE TABLE accounts (
    email NVARCHAR(50) PRIMARY KEY,
    name NVARCHAR(100),
    password NVARCHAR(100),
    register_date DATE
);

CREATE TABLE products (
    productid INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200),
    unit_price BIGINT,
    image NVARCHAR(MAX),
    description NVARCHAR(MAX),
    quantity INT,
    discount FLOAT,
    entered_date DATE,
    categoryid INT,
    brandid INT
);

CREATE TABLE orders (
    orderid INT IDENTITY(1,1) PRIMARY KEY,
    user_id NVARCHAR(50),
    adress NVARCHAR(200),
    phone NVARCHAR(20),
    amount BIGINT,
    order_date DATETIME,
    status INT,
    FOREIGN KEY (user_id) REFERENCES accounts(email)
);

CREATE TABLE payments (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id INT,
    payment_method NVARCHAR(50),
    transaction_id NVARCHAR(100),
    amount BIGINT,
    payment_status NVARCHAR(50),
    payment_date DATETIME,
    bank_code NVARCHAR(20),
    response_code NVARCHAR(10),
    description NVARCHAR(MAX),
    FOREIGN KEY (order_id) REFERENCES orders(orderid)
);
```

#### 3. Cấu hình application.properties
```properties
# Database SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=asmJava6;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123
spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.dialect=org.hibernate.dialect.SQLServer2012Dialect
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=none

# VNPay Configuration
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay-return
vnpay.tmnCode=LBJ0EVHH
vnpay.hashSecret=WN0OP4P4C23BBD98ERF14NTNK0GEYT6L

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### II. CÔNG NGHỆ SỬ DỤNG

#### 1. Backend
- **Spring Boot 2.5.2**: Framework chính
- **Spring Data JPA**: ORM với Hibernate
- **Spring Security**: Authentication & Authorization
- **SQL Server 2019**: Database
- **JavaMail**: Gửi email
- **Lombok**: Giảm boilerplate code

**Maven Dependencies:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
</dependencies>
```

#### 2. Frontend
- **AngularJS 1.8**: JavaScript framework
- **Bootstrap 4**: CSS framework
- **Chart.js 3.x**: Biểu đồ
- **SweetAlert2**: Alert đẹp
- **jQuery**: DOM manipulation
- **Owl Carousel**: Slider
- **Font Awesome**: Icons

**package.json:**
```json
{
  "dependencies": {
    "angular": "^1.8.0",
    "bootstrap": "^4.6.0",
    "chart.js": "^3.9.1",
    "sweetalert2": "^11.0.0",
    "jquery": "^3.6.0"
  }
}
```

#### 3. Tools
- **Maven**: Build tool
- **Git**: Version control
- **Postman**: API testing
- **SQL Server Management Studio**: Database management
- **Visual Studio Code**: Code editor
- **Chrome DevTools**: Debugging

### III. HƯỚNG DẪN CHẠY PROJECT

#### 1. Yêu cầu hệ thống
- Java JDK 17+
- SQL Server 2019+
- Maven 3.6+
- Node.js (optional, để cài frontend packages)

#### 2. Cài đặt database
```sql
-- Tạo database
CREATE DATABASE asmJava6;
GO

USE asmJava6;
GO

-- Chạy file tmartshop.sql để tạo bảng và insert dữ liệu mẫu
```

#### 3. Cấu hình application.properties
- Đổi `spring.datasource.username` và `password` theo SQL Server của bạn
- Đổi `spring.mail.username` và `password` theo Gmail của bạn

#### 4. Chạy project
```bash
# Compile và chạy
mvn clean install
mvn spring-boot:run

# Hoặc chạy file JAR
java -jar target/AsignmentJava6-0.0.1-SNAPSHOT.jar
```

#### 5. Truy cập
- **User**: http://localhost:8080/
- **Admin**: http://localhost:8080/admin/index
- **API Docs**: http://localhost:8080/swagger-ui.html

#### 6. Tài khoản test
**Admin:**
- Email: admin@gmail.com
- Password: 123

**User:**
- Email: user@gmail.com
- Password: 123

**VNPay Test Card:**
- Số thẻ: 9704198526191432198
- Tên: NGUYEN VAN A
- Ngày phát hành: 07/15
- OTP: 123456

---

## PHẦN 4: KẾT LUẬN

### I. KẾT QUẢ ĐẠT ĐƯỢC
1. Hoàn thành hệ thống E-commerce đầy đủ chức năng
2. Dashboard admin với biểu đồ thống kê đẹp
3. Tích hợp VNPay thanh toán online
4. Gửi email tự động
5. Responsive design
6. RESTful API chuẩn
7. Bảo mật với Spring Security

### II. HẠNG CHẾ & HƯỚNG PHÁT TRIỂN
1. **Hạn chế:**
   - Chưa có chức năng đánh giá sản phẩm
   - Chưa có chat realtime
   - Chưa tích hợp nhiều payment gateway

2. **Hướng phát triển:**
   - Thêm Momo, ZaloPay payment
   - WebSocket cho chat realtime
   - Recommendation system
   - Mobile app với React Native
   - Microservices architecture

### III. TÀI LIỆU THAM KHẢO
1. https://spring.io/projects/spring-boot
2. https://docs.spring.io/spring-security/reference/
3. https://sandbox.vnpayment.vn/apis/docs/
4. https://www.chartjs.org/docs/
5. https://getbootstrap.com/docs/
6. https://stackoverflow.com/

---

## PHỤ LỤC

### A. Source Code quan trọng
- PaymentServiceImpl.java
- VNPayConfig.java
- order.js
- statiscal.js

### B. Screenshots
- Dashboard overview
- Statistical charts
- VNPay payment flow
- Mobile responsive

### C. Database Schema
- ERD diagram
- Table structures
- Sample data

### D. API Documentation
- REST endpoints
- Request/Response examples
- Error codes

---

# CHECKLIST HOÀN THÀNH BÁO CÁO

## Dashboard (6đ)
- [ ] Mô tả hệ thống Dashboard đầy đủ (2đ)
  - [ ] Quản lý sản phẩm với screenshots
  - [ ] Quản lý danh mục, thương hiệu
  - [ ] Quản lý đơn hàng với flow xác nhận
  - [ ] Quản lý tài khoản
  - [ ] Code minh họa backend & frontend

- [ ] Biểu đồ hóa số liệu (1đ)
  - [ ] Biểu đồ doanh thu (Line Chart) với screenshots
  - [ ] Top 5 sản phẩm (Bar Chart)
  - [ ] Top 5 khách hàng (Line Chart)
  - [ ] SQL queries
  - [ ] Chart.js code

- [ ] Tính năng đột phá (1đ)
  - [ ] VNPay payment với luồng chi tiết
  - [ ] Code backend (signature, callback)
  - [ ] Code frontend (SweetAlert2, redirect)
  - [ ] Screenshots đầy đủ
  - [ ] Firebase upload
  - [ ] Email automation

- [ ] Kiểm thử (2đ)
  - [ ] Bảng test cases đầy đủ
  - [ ] Screenshots test chức năng
  - [ ] Performance testing với số liệu
  - [ ] Responsive testing trên nhiều thiết bị
  - [ ] Browser compatibility

## Browser (4đ)
- [ ] Phía người dùng (2đ)
  - [ ] Trang chủ với screenshots
  - [ ] Danh sách sản phẩm
  - [ ] Chi tiết sản phẩm
  - [ ] Giỏ hàng
  - [ ] Checkout với VNPay
  - [ ] Quản lý đơn hàng
  - [ ] Đăng nhập/ký
  - [ ] Code minh họa

- [ ] Tính năng nổi bật (1đ)
  - [ ] Ajax không reload
  - [ ] SweetAlert2
  - [ ] Filter & Sort
  - [ ] Search

- [ ] Kiểm thử UI (1đ)
  - [ ] Test cases User
  - [ ] Responsive screenshots
  - [ ] Browser compatibility

## Yêu cầu chung
- [ ] Cấu trúc dự án đầy đủ
- [ ] Database schema & ERD
- [ ] Cấu hình application.properties
- [ ] Maven pom.xml
- [ ] Công nghệ sử dụng
- [ ] Hướng dẫn chạy project
- [ ] Kết luận & hướng phát triển
- [ ] Tài liệu tham khảo

## Format Word
- [ ] Font: Times New Roman, size 13
- [ ] Line spacing: 1.5
- [ ] Margins: 2cm
- [ ] Đánh số trang
- [ ] Mục lục tự động
- [ ] Hình ảnh có caption
- [ ] Code có syntax highlighting
- [ ] Bảng biểu rõ ràng

---

**LƯU Ý QUAN TRỌNG:**

1. **Screenshots phải rõ nét**, đầy đủ màn hình, có highlight phần quan trọng
2. **Code phải format đẹp**, có comment tiếng Việt giải thích
3. **Biểu đồ phải màu sắc**, không đen trắng
4. **Test cases phải chi tiết**, có Expected vs Actual
5. **VNPay payment là tính năng KEY**, phải mô tả kỹ nhất!

**Thứ tự ưu tiên:**
1. VNPay Payment (tính năng đột phá nhất)
2. Biểu đồ thống kê (đẹp, dễ thấy)
3. Dashboard CRUD (cơ bản)
4. Kiểm thử (quan trọng để đủ điểm)

Chúc bạn làm báo cáo tốt! 🎉
