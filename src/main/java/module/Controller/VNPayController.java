package module.Controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import module.Config.VNPayConfig;
import module.DAO.OrderDAO;
import module.DAO.PaymentDAO;
import module.Domain.Order;
import module.Domain.Payment;
import module.Utils.VNPayUtil;

/**
 * VNPay Payment Controller - Style giống VNPay Demo
 * Đơn giản hóa theo cấu trúc demo của VNPay
 */
@Controller
@RequestMapping("/vnpay")
public class VNPayController {
    
    @Autowired
    private VNPayConfig vnPayConfig;
    
    @Autowired
    private PaymentDAO paymentDAO;
    
    @Autowired
    private OrderDAO orderDAO;
    
    /**
     * Tạo payment URL và redirect đến VNPay
     * URL: /vnpay/create-payment?orderID=123&amount=15000000
     */
    @GetMapping("/create-payment")
    public String createPayment(
            @RequestParam Integer orderID,
            @RequestParam Long amount,
            HttpServletRequest request,
            Model model) throws UnsupportedEncodingException {
        
        // Validate order
        Optional<Order> orderOpt = orderDAO.findById(orderID);
        if (!orderOpt.isPresent()) {
            model.addAttribute("message", "Đơn hàng không tồn tại");
            return "error";
        }
        
        Order order = orderOpt.get();
        
        // Tạo payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("VNPAY");
        payment.setAmount(amount);
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(new Date());
        payment.setDescription("Thanh toán đơn hàng #" + orderID);
        Payment savedPayment = paymentDAO.save(payment);
        
        // VNPay Parameters
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String orderType = "other";
        
        // Amount phải nhân 100
        long vnp_Amount = amount * 100;
        
        String vnp_TxnRef = savedPayment.getPaymentID().toString();
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);
        
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
        String orderInfo = "Thanh toan don hang:" + orderID;
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        // Thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // Build query URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
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
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl;
        
        // Redirect đến VNPay
        return "redirect:" + paymentUrl;
    }
    
    /**
     * VNPay IPN (Instant Payment Notification) callback
     * URL: /vnpay/return
     */
    @GetMapping("/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        Map<String, String> fields = new HashMap<>();
        
        // Get all params
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        // Verify signature
        String signValue = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret());
        
        if (signValue.equals(vnp_SecureHash)) {
            // Signature hợp lệ
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_BankCode = request.getParameter("vnp_BankCode");
            
            // Update payment
            try {
                Long paymentID = Long.parseLong(vnp_TxnRef);
                Optional<Payment> paymentOpt = paymentDAO.findById(paymentID);
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setTransactionID(vnp_TransactionNo);
                    payment.setBankCode(vnp_BankCode);
                    payment.setResponseCode(vnp_ResponseCode);
                    
                    if ("00".equals(vnp_ResponseCode)) {
                        // Thành công
                        payment.setPaymentStatus("SUCCESS");
                        
                        // Update order status
                        Order order = payment.getOrder();
                        if (order.getStatus() == 1) {
                            order.setStatus(2); // Đã xác nhận
                            orderDAO.save(order);
                        }
                        
                        model.addAttribute("status", "success");
                        model.addAttribute("message", "Thanh toán thành công!");
                    } else {
                        // Thất bại
                        payment.setPaymentStatus("FAILED");
                        payment.setDescription("Giao dịch thất bại: " + getResponseMessage(vnp_ResponseCode));
                        
                        model.addAttribute("status", "failed");
                        model.addAttribute("message", getResponseMessage(vnp_ResponseCode));
                    }
                    
                    paymentDAO.save(payment);
                    model.addAttribute("payment", payment);
                    model.addAttribute("order", payment.getOrder());
                }
            } catch (Exception e) {
                model.addAttribute("status", "error");
                model.addAttribute("message", "Lỗi xử lý: " + e.getMessage());
            }
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Chữ ký không hợp lệ");
        }
        
        return "Usersform/payment-result";
    }
    
    /**
     * Get response message từ VNPay response code
     */
    private String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
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
