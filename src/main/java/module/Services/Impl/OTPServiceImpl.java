package module.Services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import module.DAO.AccountDAO;
import module.Services.EmailService;
import module.Services.OTPService;
import module.Services.SessionService;

/**
 * Implementation của OTPService
 * Xử lý toàn bộ logic nghiệp vụ liên quan đến OTP
 */
@Service
public class OTPServiceImpl implements OTPService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private AccountDAO accountDAO;

    @Override
    public int generateOTP() {
        // Generate số ngẫu nhiên từ 100000 đến 999999
        return (int) Math.floor(Math.random() * (999999 - 100000 + 1) + 100000);
    }

    @Override
    public int sendOTPRegister(String email, String subject) throws Exception {
        // Kiểm tra email đã tồn tại chưa
        if (accountDAO.existsById(email)) {
            throw new Exception("Email đã tồn tại trong hệ thống");
        }
        
        // Generate OTP
        int otp = generateOTP();
        
        // Lưu vào session
        sessionService.set("otp", otp);
        
        // Tạo nội dung email
        String body = "Mã OTP CỦA BẠN LÀ: " + otp;
        
        // Gửi email
        emailService.sendmail(email, subject, body);
        
        return otp;
    }

    @Override
    public int sendOTPForgotPassword(String email, String subject) throws Exception {
        // Kiểm tra email có tồn tại không
        if (!accountDAO.existsById(email)) {
            throw new Exception("Email không tồn tại trong hệ thống");
        }
        
        // Generate OTP
        int otp = generateOTP();
        
        // Lưu vào session
        sessionService.set("otpforgot", otp);
        
        // Tạo nội dung email
        String body = "Mã OTP ĐỂ LẤY LẠI MẬT KHẨU LÀ: " + otp;
        
        // Gửi email
        emailService.sendmail(email, subject, body);
        
        return otp;
    }

    @Override
    public int getOTPFromSession() {
        try {
            // Lấy OTP từ session
            Integer otp = (Integer) sessionService.get("otp");
            return otp != null ? otp : generateRandomInvalidOTP();
        } catch (Exception e) {
            // Trả về số ngẫu nhiên 7 chữ số nếu không tìm thấy
            return generateRandomInvalidOTP();
        }
    }

    @Override
    public int getOTPForgotFromSession() {
        try {
            // Lấy OTP quên mật khẩu từ session
            Integer otp = (Integer) sessionService.get("otpforgot");
            return otp != null ? otp : generateRandomInvalidOTP();
        } catch (Exception e) {
            // Trả về số ngẫu nhiên 7 chữ số nếu không tìm thấy
            return generateRandomInvalidOTP();
        }
    }

    @Override
    public String removeOTPSession() {
        // Xóa OTP đăng ký
        sessionService.remove("otp");
        
        // Xóa OTP quên mật khẩu
        sessionService.remove("otpforgot");
        
        return "Thanh Cong";
    }

    @Override
    public boolean validateOTP(int inputOTP) {
        int sessionOTP = getOTPFromSession();
        return inputOTP == sessionOTP;
    }

    @Override
    public boolean validateOTPForgot(int inputOTP) {
        int sessionOTP = getOTPForgotFromSession();
        return inputOTP == sessionOTP;
    }
    
    /**
     * Generate số ngẫu nhiên 7 chữ số (invalid OTP)
     * Dùng để trả về khi không tìm thấy OTP trong session
     */
    private int generateRandomInvalidOTP() {
        return (int) Math.floor(Math.random() * (9999999 - 1000000 + 1) + 1000000);
    }
}
