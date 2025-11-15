package module.RestController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import module.DAO.AccountRoleDAO;
import module.Domain.Account;
import module.Domain.AccountRoles;
import module.Domain.Role;
import module.Services.AccountService;

@CrossOrigin("*")
@RestController
@RequestMapping("restAccount")
public class AccountRestController {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	AccountRoleDAO Dao;

	@GetMapping("/accounts")
	public ResponseEntity<List<Account>> getAll() {
		return ResponseEntity.ok(accountService.getAllAccounts());
	}

	@GetMapping("/accountRole")
	public ResponseEntity<List<AccountRoles>> getAllAccRole() {
		return ResponseEntity.ok(accountService.getAllAccountRoles());
	}

	@GetMapping("/Role")
	public ResponseEntity<List<Role>> getRole() {
		return ResponseEntity.ok(accountService.getAllRoles());
	}

	@GetMapping("/accounts/{username}")
	public ResponseEntity<Account> getOne(@PathVariable("username") String username) {
		Account account = accountService.getAccountByEmail(username);
		if (account == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(account);
	}
	
	@GetMapping("/accountss/{username}")
	public ResponseEntity<Account> getOneAccount(@PathVariable("username") String username) {
		Account account = accountService.getAccountByEmailWithoutEncryption(username);
		if (account == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(account);
	}
	
	@GetMapping("/Role/USER")
	public ResponseEntity<Role> getOneRole() {
		return ResponseEntity.ok(accountService.getUserRole());
	}

	@PostMapping("/accounts")
	public ResponseEntity<Account> Post(@RequestBody Account account) {
		try {
			Account created = accountService.createAccount(account);
			return ResponseEntity.ok(created);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/accountRole")
	public ResponseEntity<AccountRoles> PostAccRole(@RequestBody AccountRoles accountRole) {
		AccountRoles created = accountService.createAccountRole(accountRole);
		return ResponseEntity.ok(created);
	}

	@PutMapping("/accounts/{username}")
	public ResponseEntity<Account> Put(@PathVariable("username") String username, @RequestBody Account account) {
		try {
			Account updated = accountService.updateAccount(username, account);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/accounts/{username}")
	public ResponseEntity<Void> Delete(@PathVariable("username") String username) {
		try {
			accountService.deleteAccount(username);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/accountRole/{id}")
	public ResponseEntity<Void> DeleteAccRole(@PathVariable("id") Integer id) {
		if (!Dao.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		Dao.deleteById(id);
		return ResponseEntity.ok().build();
	}

}
