/*
package com.check_payment_due_service.model.entity;

import com.common_service.model.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_due")
public class PaymentDue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceId; // e.g., bill/loan ID
    private String description;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    private PaymentDueStatus status; // PENDING, PAID, OVERDUE, CANCELLED

    private LocalDate dueDate;
    private LocalDate paidDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "paymentDue", cascade = CascadeType.ALL)
    private PaymentSchedule paymentSchedule;

    @OneToMany(mappedBy = "paymentDue")
    private List<Payment> payments;

    @OneToMany(mappedBy = "paymentDue")
    private List<PaymentReminder> reminders;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant; // Optional for third-party bills
}*/
