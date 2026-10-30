
package com.kasir.amana.Config;

import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(
                    passwordEncoder.encode("Admin123!")
                );
                admin.setFullName("Administrator");
                admin.setRole("ADMIN");
                admin.setActive(true);

                userRepository.save(admin);

                System.out.println("Akun Admin berhasil dibuat.");
            }
        };
    }
}
