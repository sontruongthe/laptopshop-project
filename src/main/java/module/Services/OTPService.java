package module.Services;

/**
 * Service xử lý nghiệp vụ OTP (One-Time Password)
 * Tách logic ra khỏi Controller để dễ bảo trì và test
 */
public interface OTPService {
    
    /**
     * Generate mã OTP ngẫu nhiên 6 chữ số
     * @return int - Mã OTP (100000 - 999999)
     */
    int generateOTP();
    
    /**
     * Gửi OTP đăng ký tài khoản qua email
     * @param email - Email người nhận
     * @param subject - Tiêu đề email
     * @return int - Mã OTP đã gửi
     * @throws Exception - Nếu email đã tồn tại trong hệ thống
     */
    int sendOTPRegister(String email, String subject) throws Exception;
    
    /**
     * Gửi OTP quên mật khẩu qua email
     * @param email - Email người nhận
     * @param subject - Tiêu đề email
     * @return int - Mã OTP đã gửi
     * @throws Exception - Nếu email không tồn tại trong hệ thống
     */
    int sendOTPForgotPassword(String email, String subject) throws Exception;
    
    /**
     * Lấy mã OTP đăng ký từ session
     * @return int - Mã OTP hoặc số ngẫu nhiên 7 chữ số nếu không tìm thấy
     */
    int getOTPFromSession();
    
    /**
     * Lấy mã OTP quên mật khẩu từ session
     * @return int - Mã OTP hoặc số ngẫu nhiên 7 chữ số nếu không tìm thấy
     */
    int getOTPForgotFromSession();
    
    /**
     * Xóa tất cả OTP khỏi session
     * @return String - "Thanh Cong"
     */
    String removeOTPSession();
    
    /**
     * Validate OTP đăng ký
     * @param inputOTP - OTP người dùng nhập
     * @return boolean - true nếu hợp lệ, false nếu không
     */
    boolean validateOTP(int inputOTP);
    
    /**
     * Validate OTP quên mật khẩu
     * @param inputOTP - OTP người dùng nhập
     * @return boolean - true nếu hợp lệ, false nếu không
     */
    boolean validateOTPForgot(int inputOTP);
}
