package xyz.waranim.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.waranim.common.BaseEntity;

@Entity
@Table(name = "receipts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReceiptEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String yookassaReceiptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    private String type;

    @Enumerated(EnumType.STRING)
    private StatusReceipt status;
}
