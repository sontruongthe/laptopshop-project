package module.Services;

import java.util.List;
import module.Domain.Account;
import module.Domain.AccountRoles;
import module.Domain.Role;

/**
 * Service xử lý nghiệp vụ liên quan đến Account
 * Tách logic ra khỏi Controller để dễ bảo trì và test
 */
public interface AccountService {
    
    /**
     * Lấy danh sách tất cả tài khoản
     * @return List<Account> - Danh sách account
     */
    List<Account> getAllAccounts();
    
    /**
     * Lấy danh sách tất cả AccountRole
     * @return List<AccountRoles> - Danh sách AccountRole
     */
    List<AccountRoles> getAllAccountRoles();
    
    /**
     * Lấy danh sách tất cả Role
     * @return List<Role> - Danh sách Role (USER, ADMIN)
     */
    List<Role> getAllRoles();
    
    /**
     * Lấy thông tin account theo email (với password đã mã hóa)
     * @param email - Email của account
     * @return Account - Thông tin account hoặc null nếu không tồn tại
     */
    Account getAccountByEmail(String email);
    
    /**
     * Lấy thông tin account theo email (không mã hóa password)
     * @param email - Email của account
     * @return Account - Thông tin account hoặc null nếu không tồn tại
     */
    Account getAccountByEmailWithoutEncryption(String email);
    
    /**
     * Lấy Role USER mặc định
     * @return Role - Role USER
     */
    Role getUserRole();
    
    /**
     * Tạo account mới
     * @param account - Thông tin account cần tạo
     * @return Account - Account đã được tạo
     * @throws Exception - Nếu email đã tồn tại
     */
    Account createAccount(Account account) throws Exception;
    
    /**
     * Tạo AccountRole mới
     * @param accountRole - Thông tin AccountRole cần tạo
     * @return AccountRoles - AccountRole đã được tạo
     */
    AccountRoles createAccountRole(AccountRoles accountRole);
    
    /**
     * Cập nhật thông tin account
     * @param email - Email của account cần cập nhật
     * @param account - Thông tin mới
     * @return Account - Account đã được cập nhật
     * @throws Exception - Nếu account không tồn tại
     */
    Account updateAccount(String email, Account account) throws Exception;
    
    /**
     * Xóa account theo email
     * @param email - Email của account cần xóa
     * @throws Exception - Nếu account không tồn tại
     */
    void deleteAccount(String email) throws Exception;
    
    /**
     * Kiểm tra account có tồn tại không
     * @param email - Email cần kiểm tra
     * @return boolean - true nếu tồn tại, false nếu không
     */
    boolean accountExists(String email);
}
