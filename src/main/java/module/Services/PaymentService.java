package module.Services;

import java.util.List;
import java.util.Map;

import module.Domain.Payment;

/**
 * Service xử lý nghiệp vụ thanh toán online
 * Tích hợp VNPay Payment Gateway
 */
public interface PaymentService {
    
    /**
     * Tạo URL thanh toán VNPay
     * @param orderID - ID đơn hàng
     * @param amount - Số tiền (VNĐ)
     * @param orderInfo - Mô tả đơn hàng
     * @param ipAddress - IP của khách hàng
     * @return String - URL redirect đến VNPay
     */
    String createVNPayPaymentUrl(Integer orderID, Long amount, String orderInfo, String ipAddress) throws Exception;
    
    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * @param params - Parameters từ VNPay return
     * @return Payment - Thông tin giao dịch đã xử lý
     */
    Payment processVNPayReturn(Map<String, String> params) throws Exception;
    
    /**
     * Tạo payment record khi khách chọn COD (Cash on Delivery)
     * @param orderID - ID đơn hàng
     * @param amount - Số tiền
     * @return Payment - Thông tin payment COD
     */
    Payment createCODPayment(Integer orderID, Long amount) throws Exception;
    
    /**
     * Lấy thông tin payment theo transactionID
     */
    Payment getPaymentByTransactionID(String transactionID);
    
    /**
     * Lấy tất cả payment của một order
     */
    List<Payment> getPaymentsByOrderID(Integer orderID);
    
    /**
     * Kiểm tra order đã thanh toán thành công chưa
     */
    boolean isOrderPaid(Integer orderID);
    
    /**
     * Hủy payment (update status thành FAILED)
     */
    Payment cancelPayment(Long paymentID) throws Exception;
}
