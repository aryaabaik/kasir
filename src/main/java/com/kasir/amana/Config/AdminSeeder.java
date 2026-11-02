
package com.kasir.amana.Config;

import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {

        return args -> {
            // Only seed if no admin user exists
            if (userRepository.findByUsername("admin").isEmpty()) {

                // Get credentials from environment variables
                String adminUsername = environment.getProperty("ADMIN_USERNAME", "admin");
                String adminPassword = environment.getProperty("ADMIN_PASSWORD");
                String adminFullName = environment.getProperty("ADMIN_FULLNAME", "Administrator");

                // If no password provided in environment, generate a secure random one
                if (adminPassword == null || adminPassword.isBlank()) {
                    adminPassword = generateSecurePassword();
                    log.warn("═══════════════════════════════════════════════════════════");
                    log.warn("INITIAL ADMIN ACCOUNT CREATED");
                    log.warn("═══════════════════════════════════════════════════════════");
                    log.warn("Username: {}", adminUsername);
                    log.warn("Password: {}", adminPassword);
                    log.warn("═══════════════════════════════════════════════════════════");
                    log.warn("IMPORTANT: Change this password immediately after first login!");
                    log.warn("Set ADMIN_PASSWORD environment variable before next startup.");
                    log.warn("═══════════════════════════════════════════════════════════");
                } else {
                    log.info("Creating admin account from environment configuration.");
                }

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setFullName(adminFullName);
                admin.setRole("ADMIN");
                admin.setActive(true);

                userRepository.save(admin);

                log.info("Admin account '{}' created successfully.", adminUsername);
            } else {
                log.debug("Admin account already exists. Skipping seeder.");
            }
        };
    }

    /**
     * Generates a cryptographically secure random password.
     * Format: 16 characters, Base64-encoded for readability.
     * 
     * @return A secure random password
     */
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

