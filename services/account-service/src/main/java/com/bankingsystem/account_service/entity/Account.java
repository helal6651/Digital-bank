package com.bankingsystem.account_service.entity;

import com.bankingsystem.account_service.model.Currency;
import jakarta.persistence.*;
import lombok.*;
import com.common_service.model.entity.User;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Accounts")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_account"))
    private User user;

    @Column(name = "account_number", nullable = false, unique = true, length = 20, updatable = false)
    private String accountNumber; // Auto-generated

    @Column(name = "account_name", nullable = false, length = 30)
    private String accountName;
    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Currency currency = Currency.BDT;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * Automatically sets the timestamps when an entity is created.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();

        // Generate unique account number if not already set
        if (this.accountNumber == null || this.accountNumber.isEmpty()) {
            this.accountNumber = generateUniqueAccountNumber();
        }
    }

    /**
     * Automatically updates the timestamp when an entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Generates a unique account number (e.g., 20-character alphanumeric).
     *
     * @return A randomly generated unique account number.
     */
    private String generateUniqueAccountNumber() {
        // Use UUID, remove dashes, and limit to 20 characters
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}