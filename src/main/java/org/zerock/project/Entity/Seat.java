package org.zerock.project.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trn_seat",
                        columnNames = {"trn_id", "seat_number"}
                )
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer seatId;

    // 🔹 열차(Train_info)와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trn_id",
            nullable = false
    )
    private Train_info train;

    @Column(name = "seat_number", nullable = false, length = 5)
    private String seatNumber;

    @Column(name = "status", nullable = false, length = 15)
    private String status; // AVAILABLE / RESERVED / BOOKED

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ======================
       생명주기 콜백
       ====================== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    /* ======================
       Getter / Setter
       ====================== */

    public Integer getSeatId() {
        return seatId;
    }

    public Train_info getTrain() {
        return train;
    }

    public void setTrain(Train_info train) {
        this.train = train;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }
}

