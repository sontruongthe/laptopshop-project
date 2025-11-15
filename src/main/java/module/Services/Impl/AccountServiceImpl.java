package module.Services.Impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import module.DAO.AccountDAO;
import module.DAO.AccountRoleDAO;
import module.DAO.RoleDAO;
import module.Domain.Account;
import module.Domain.AccountRoles;
import module.Domain.Role;
import module.Services.AccountService;

/**
 * Implementation của AccountService
 * Xử lý toàn bộ logic nghiệp vụ liên quan đến Account
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDAO accountDAO;
    
    @Autowired
    private AccountRoleDAO accountRoleDAO;
    
    @Autowired
    private RoleDAO roleDAO;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }

    @Override
    public List<AccountRoles> getAllAccountRoles() {
        return accountRoleDAO.findAll();
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDAO.findAll();
    }

    @Override
    public Account getAccountByEmail(String email) {
        if (!accountDAO.existsById(email)) {
            return null;
        }
        Account account = accountDAO.findById(email).get();
        // Mã hóa password trước khi trả về
        account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
        return account;
    }

    @Override
    public Account getAccountByEmailWithoutEncryption(String email) {
        if (!accountDAO.existsById(email)) {
            return null;
        }
        return accountDAO.findById(email).get();
    }

    @Override
    public Role getUserRole() {
        return roleDAO.findById("USER").orElse(null);
    }

    @Override
    public Account createAccount(Account account) throws Exception {
        // Kiểm tra email đã tồn tại chưa
        if (accountDAO.existsById(account.getEmail())) {
            throw new Exception("Email đã tồn tại trong hệ thống");
        }
        
        // Lưu account vào database
        return accountDAO.save(account);
    }

    @Override
    public AccountRoles createAccountRole(AccountRoles accountRole) {
        return accountRoleDAO.save(accountRole);
    }

    @Override
    public Account updateAccount(String email, Account account) throws Exception {
        // Kiểm tra account có tồn tại không
        if (!accountDAO.existsById(email)) {
            throw new Exception("Account không tồn tại");
        }
        
        // Cập nhật account
        return accountDAO.save(account);
    }

    @Override
    public void deleteAccount(String email) throws Exception {
        // Kiểm tra account có tồn tại không
        if (!accountDAO.existsById(email)) {
            throw new Exception("Account không tồn tại");
        }
        
        // Xóa account
        accountDAO.deleteById(email);
    }

    @Override
    public boolean accountExists(String email) {
        return accountDAO.existsById(email);
    }
}
