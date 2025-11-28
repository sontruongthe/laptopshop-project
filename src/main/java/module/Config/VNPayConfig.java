package module.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình VNPay Payment Gateway
 * Docs: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
 */
@Configuration
public class VNPayConfig {
    
    // VNPay URL
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;
    
    @Value("${vnpay.returnUrl:http://localhost:8080/payment/vnpay-return}")
    private String returnUrl;
    
    @Value("${vnpay.tmnCode:DEMO}")
    private String tmnCode;
    
    @Value("${vnpay.hashSecret:DEMOSECRETKEY}")
    private String hashSecret;
    
    @Value("${vnpay.apiUrl:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}")
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
