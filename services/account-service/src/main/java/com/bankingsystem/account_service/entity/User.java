package com.bankingsystem.account_service.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users")
@Data // Generates getters, setters, equals, hashCode, toString, etc.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // Maps to SERIAL PRIMARY KEY

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName; // Full name of the user

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email; // Email address (must be unique)

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber; // Phone number (must be unique)

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth; // Date of birth of the user

    @Column(name = "address")
    private String address; // Optional address field

    @Column(name = "role", nullable = false, length = 20)
    private String role = "user"; // Default role is 'user'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * One-to-Many relationship: A user can have multiple accounts.
     * This establishes the connection with the `Accounts` entity.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts;

    /**
     * Pre-persist method to set default timestamps before saving a new record.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters are automatically handled by Lombok (@Data annotation)
}