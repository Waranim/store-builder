package xyz.waranim.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.waranim.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaymentEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String yookassaPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "storeId")
    private ShopCredentialEntity shop;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private StatusPayment status;

    private String confirmationUrl;

}
