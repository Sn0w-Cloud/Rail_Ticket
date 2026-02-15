package org.zerock.project.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Purchase_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase_list {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id")
    private int listId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member_info memberInfo;

    @Column(name = "list_name", nullable = false, length = 500)
    private String listName;

    @Column(name = "price", nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trn_id", nullable = false)
    private Train_info trainInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "merchant_pay_key", unique = true)
    private String merchantPayKey;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate; // 구매일

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseStatus status = PurchaseStatus.AVAILABLE;

    public enum PurchaseStatus {
        AVAILABLE,  // 취소가능
        COMPLETED,  // 구매확정
        CANCELED    // 취소
    }
}