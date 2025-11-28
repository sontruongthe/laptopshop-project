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

/**
 * REST API Controller cho Payment
 * Xử lý thanh toán online và COD
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/payment")
public class PaymentRestController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * API tạo URL thanh toán VNPay
     * POST /payment/create-vnpay-url
     * Body: { "orderID": 123, "amount": 15000000, "orderInfo": "Thanh toan don hang #123" }
     */
    @PostMapping("/create-vnpay-url")
    public ResponseEntity<?> createVNPayUrl(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Integer orderID = (Integer) payload.get("orderID");
            Long amount = Long.parseLong(payload.get("amount").toString());
            String orderInfo = (String) payload.get("orderInfo");
            
            // Lấy IP của client
            String ipAddress = getIpAddress(request);
            
            String paymentUrl = paymentService.createVNPayPaymentUrl(orderID, amount, orderInfo, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("message", "Tạo URL thanh toán thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", payment);
            response.put("message", "Tạo đơn hàng COD thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
     * API lấy danh sách payment của order
     * GET /payment/by-order/{orderID}
     */
    @GetMapping("/by-order/{orderID}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderID(@PathVariable Integer orderID) {
        List<Payment> payments = paymentService.getPaymentsByOrderID(orderID);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * API hủy payment
     * PUT /payment/cancel/{paymentID}
     */
    @PutMapping("/cancel/{paymentID}")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentID) {
        try {
            Payment payment = paymentService.cancelPayment(paymentID);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", payment);
            response.put("message", "Hủy thanh toán thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    // Helper method: Lấy IP address của client
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Convert IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }
}
