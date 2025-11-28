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

/**
 * Controller xử lý redirect từ VNPay
 * Hiển thị kết quả thanh toán cho user
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * VNPay sẽ redirect về URL này sau khi thanh toán
     * URL: /payment/vnpay-return?vnp_ResponseCode=00&vnp_TxnRef=123&...
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            // Lấy tất cả parameters từ VNPay
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            
            for (String key : requestParams.keySet()) {
                String value = requestParams.get(key)[0];
                params.put(key, value);
            }
            
            // Process payment return
            Payment payment = paymentService.processVNPayReturn(params);
            
            // Truyền dữ liệu sang view
            model.addAttribute("payment", payment);
            model.addAttribute("order", payment.getOrder());
            
            if ("SUCCESS".equals(payment.getPaymentStatus())) {
                model.addAttribute("status", "success");
                model.addAttribute("message", "Thanh toán thành công!");
                // Đánh dấu để JavaScript biết cần xóa giỏ hàng
                model.addAttribute("shouldClearCart", true);
            } else {
                model.addAttribute("status", "failed");
                model.addAttribute("message", payment.getDescription());
                model.addAttribute("shouldClearCart", false);
            }
            
            return "Usersform/payment-result";
            
        } catch (Exception e) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("shouldClearCart", false);
            return "Usersform/payment-result";
        }
    }
}
