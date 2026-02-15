package org.zerock.project.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.zerock.project.Entity.enums.BoardType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Integer boardId;

    // ✅ 게시판 타입은 enum 하나만
    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", length = 20, nullable = false)
    private BoardType boardType; //공지사항, 이벤트, 건의사항

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    /* ===== 작성자 ===== */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "writer_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    private Member_info writer;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /* ===== 게시글 : 답변 = 1 : N ===== */
    @OneToMany(
            mappedBy = "board",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BoardReply> replies;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
