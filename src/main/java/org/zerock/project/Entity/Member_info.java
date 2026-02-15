package org.zerock.project.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Member_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member_info {

    @Id
    @Column(name = "user_id", length = 15)
    private String userId;

    @Column(name = "user_pw", nullable = false, length = 100)
    private String userPw;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "user_address", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @CreationTimestamp // [필수 변경] 가입 시각 자동 생성
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.active;

    @Version
    private Long version;

    /* ===== enum ===== */
    public enum MemberType {
        user, admin
    }

    public enum MemberStatus {
        active, inactive, deleted
    }

}
