package module.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import module.Domain.Payment;

/**
 * DAO cho Payment - Truy vấn dữ liệu thanh toán
 */
public interface PaymentDAO extends JpaRepository<Payment, Long> {
    
    /**
     * Tìm payment theo transactionID của VNPay
     */
    Payment findByTransactionID(String transactionID);
    
    /**
     * Tìm tất cả payment của một order
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderID = ?1")
    List<Payment> findByOrderID(Integer orderID);
    
    /**
     * Tìm payment theo orderID và status
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderID = ?1 AND p.paymentStatus = ?2")
    Payment findByOrderIDAndStatus(Integer orderID, String status);
    
    /**
     * Đếm số payment thành công
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    Long countSuccessPayments();
}
