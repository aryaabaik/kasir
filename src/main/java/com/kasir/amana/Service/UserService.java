package com.kasir.amana.Service;

import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.TransactionRepository;
import com.kasir.amana.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository repository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User createCashier(
        String username,
        String fullName,
        String password) {

        username = username.trim();
        fullName = fullName.trim();

        if (username.isBlank() || fullName.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Semua field wajib diisi.");
        }

        if (repository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username sudah digunakan.");
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("CASHIER");
        user.setActive(true);

        return repository.save(user);
    }

    public CashierDeletionResult deleteCashier(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            return CashierDeletionResult.NOT_FOUND;
        }

        if (!"CASHIER".equalsIgnoreCase(user.get().getRole())) {
            return CashierDeletionResult.NOT_CASHIER;
        }

        if (transactionRepository.existsByCashier_Id(id)) {
            return CashierDeletionResult.HAS_TRANSACTIONS;
        }

        repository.delete(user.get());
        return CashierDeletionResult.DELETED;
    }

    public CashierStatusResult toggleCashierStatus(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            return CashierStatusResult.NOT_FOUND;
        }

        if (!"CASHIER".equalsIgnoreCase(user.get().getRole())) {
            return CashierStatusResult.NOT_CASHIER;
        }

        boolean isActive = Boolean.TRUE.equals(user.get().getActive());
        user.get().setActive(!isActive);
        repository.save(user.get());

        return isActive
                ? CashierStatusResult.DEACTIVATED
                : CashierStatusResult.ACTIVATED;
    }

    public enum CashierDeletionResult {
        DELETED,
        NOT_FOUND,
        NOT_CASHIER,
        HAS_TRANSACTIONS
    }

    public enum CashierStatusResult {
        ACTIVATED,
        DEACTIVATED,
        NOT_FOUND,
        NOT_CASHIER
    }

    public User updateUser(Long id, String username, String fullName) {
        username = username.trim();
        fullName = fullName.trim();

        if (username.isBlank() || fullName.isBlank()) {
            throw new IllegalArgumentException("Semua field wajib diisi.");
        }

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pengguna tidak ditemukan."));

        Optional<User> existing = repository.findByUsername(username);

        if (existing.isPresent()
                && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username sudah digunakan.");
        }

        user.setUsername(username);
        user.setFullName(fullName);

        return repository.save(user);
    }

    public User toggleActive(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pengguna tidak ditemukan."));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "Akun admin tidak bisa dinonaktifkan melalui fitur ini.");
        }

        user.setActive(!Boolean.TRUE.equals(user.getActive()));

        return repository.save(user);
    }
}
