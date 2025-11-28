package module.Domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entity Payment - Lưu thông tin giao dịch thanh toán
 */
@Entity
@Table(name = "Payments")
public class Payment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentID;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties(value = {"payments", "orderDetails"})
    private Order order;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // COD, VNPAY, MOMO
    
    @Column(name = "transaction_id", length = 100)
    private String transactionID; // Mã giao dịch từ VNPay
    
    @Column(name = "amount")
    private Long amount; // Số tiền (VNĐ)
    
    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    
    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate = new Date();
    
    @Column(name = "bank_code", length = 50)
    private String bankCode; // Mã ngân hàng
    
    @Column(name = "response_code", length = 10)
    private String responseCode; // Mã phản hồi từ VNPay
    
    @Column(name = "description", length = 500)
    private String description;
    
    // Constructors
    public Payment() {
    }
    
    // Getters and Setters
    public Long getPaymentID() {
        return paymentID;
    }
    
    public void setPaymentID(Long paymentID) {
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
