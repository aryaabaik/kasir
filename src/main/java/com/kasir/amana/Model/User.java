package com.kasir.amana.Model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class User {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column (nullable = false, length = 100)
    private String fullName;

    @Column (nullable = false, length = 20)
    private String role = "CASHIER";

    @Column (nullable = false)
    private Boolean active = true;

    @Column (nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
}
