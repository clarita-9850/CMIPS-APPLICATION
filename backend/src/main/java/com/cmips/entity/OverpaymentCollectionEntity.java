package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Overpayment Collection (personal payment records)
 *
 * Records personal payment installments against an overpayment recovery.
 * When collection equals the Balance, status is automatically set to Closed.
 * Collections are always dollar-based; dollars converted to hours when applicable.
 */
@Entity
@Table(name = "overpayment_collections", indexes = {
    @Index(name = "idx_ovcoll_overpayment", columnList = "overpayment_id"),
    @Index(name = "idx_ovcoll_date", columnList = "collection_date")
})
@Data
@NoArgsConstructor
public class OverpaymentCollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "overpayment_id", nullable = false)
    private Long overpaymentId;

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** PERSONAL_PAYMENT or PAYROLL_DEDUCTION */
    @Column(name = "mode_of_payment", length = 30)
    @Enumerated(EnumType.STRING)
    private ModeOfPayment modeOfPayment;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ModeOfPayment {
        PERSONAL_PAYMENT,
        PAYROLL_DEDUCTION
    }
}
