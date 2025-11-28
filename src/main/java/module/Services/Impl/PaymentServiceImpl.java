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

/**
 * Implementation của PaymentService
 * Xử lý thanh toán VNPay và COD
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentDAO paymentDAO;
    
    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private VNPayConfig vnPayConfig;
    
    @Override
    public String createVNPayPaymentUrl(Integer orderID, Long amount, String orderInfo, String ipAddress) throws Exception {
        // Kiểm tra order tồn tại
        Optional<Order> orderOpt = orderDAO.findById(orderID);
        if (!orderOpt.isPresent()) {
            throw new Exception("Order không tồn tại");
        }
        Order order = orderOpt.get();
        
        // Tạo payment record với status PENDING
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("VNPAY");
        payment.setAmount(amount);
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(new Date());
        payment.setDescription("Thanh toán đơn hàng #" + orderID);
        
        Payment savedPayment = paymentDAO.save(payment);
        
        // Build VNPay parameters
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu x100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", savedPayment.getPaymentID().toString()); // Dùng paymentID làm reference
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        // Thêm thời gian tạo và hết hạn
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Build query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data - fieldName KHÔNG encode, fieldValue encode US-ASCII (theo code VNPay)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query - CẢ fieldName và fieldValue đều encode US-ASCII (theo code VNPay)
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
        String hashDataStr = hashData.toString();
        
        // Debug logging
        System.out.println("=== VNPay Payment URL Debug ===");
        System.out.println("HashSecret: " + vnPayConfig.getHashSecret());
        System.out.println("HashData: " + hashDataStr);
        
        String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashDataStr);
        System.out.println("Generated Hash: " + vnpSecureHash);
        
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl;
        
        System.out.println("Payment URL: " + paymentUrl);
        System.out.println("===============================");
        
        return paymentUrl;
    }
    
    @Override
    public Payment processVNPayReturn(Map<String, String> params) throws Exception {
        // Lấy secure hash từ VNPay
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");
        
        // Verify signature
        String signValue = hashAllFields(params);
        
        if (!signValue.equals(vnpSecureHash)) {
            throw new Exception("Invalid signature");
        }
        
        // Lấy payment record
        String txnRef = params.get("vnp_TxnRef");
        Long paymentID = Long.parseLong(txnRef);
        Optional<Payment> paymentOpt = paymentDAO.findById(paymentID);
        
        if (!paymentOpt.isPresent()) {
            throw new Exception("Payment không tồn tại");
        }
        
        Payment payment = paymentOpt.get();
        
        // Update payment info
        String responseCode = params.get("vnp_ResponseCode");
        payment.setResponseCode(responseCode);
        payment.setTransactionID(params.get("vnp_TransactionNo"));
        payment.setBankCode(params.get("vnp_BankCode"));
        
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            payment.setPaymentStatus("SUCCESS");
            
            // Update order status từ PENDING (0) sang CONFIRMED (1)
            Order order = payment.getOrder();
            if (order.getStatus() == 0) { // Chỉ update nếu đang ở trạng thái PENDING
                order.setStatus(1); // Chuyển sang "Đã xác nhận"
                orderDAO.save(order);
            }
        } else {
            // Thanh toán thất bại
            payment.setPaymentStatus("FAILED");
            payment.setDescription("Thanh toán thất bại: " + getResponseMessage(responseCode));
        }
        
        return paymentDAO.save(payment);
    }
    
    @Override
    public Payment createCODPayment(Integer orderID, Long amount) throws Exception {
        Optional<Order> orderOpt = orderDAO.findById(orderID);
        if (!orderOpt.isPresent()) {
            throw new Exception("Order không tồn tại");
        }
        
        Payment payment = new Payment();
        payment.setOrder(orderOpt.get());
        payment.setPaymentMethod("COD");
        payment.setAmount(amount);
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(new Date());
        payment.setDescription("Thanh toán khi nhận hàng (COD)");
        
        return paymentDAO.save(payment);
    }
    
    @Override
    public Payment getPaymentByTransactionID(String transactionID) {
        return paymentDAO.findByTransactionID(transactionID);
    }
    
    @Override
    public List<Payment> getPaymentsByOrderID(Integer orderID) {
        return paymentDAO.findByOrderID(orderID);
    }
    
    @Override
    public boolean isOrderPaid(Integer orderID) {
        Payment payment = paymentDAO.findByOrderIDAndStatus(orderID, "SUCCESS");
        return payment != null;
    }
    
    @Override
    public Payment cancelPayment(Long paymentID) throws Exception {
        Optional<Payment> paymentOpt = paymentDAO.findById(paymentID);
        if (!paymentOpt.isPresent()) {
            throw new Exception("Payment không tồn tại");
        }
        
        Payment payment = paymentOpt.get();
        payment.setPaymentStatus("CANCELLED");
        payment.setDescription("Đã hủy thanh toán");
        
        return paymentDAO.save(payment);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Tạo HMAC SHA512 signature
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Hash tất cả fields để verify signature
     * VNPay trả về params đã decoded, nên hash KHÔNG encode
     */
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
                
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        return hmacSHA512(vnPayConfig.getHashSecret(), sb.toString());
    }
    
    /**
     * Lấy message từ response code của VNPay
     */
    private String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default: return "Giao dịch thất bại";
        }
    }
}
