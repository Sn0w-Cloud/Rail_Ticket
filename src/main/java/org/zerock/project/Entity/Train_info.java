package org.zerock.project.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Train_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Train_info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 설정
    @Column(name = "trn_id")
    private Long trnId;

    @Column(name = "train_no", nullable = false)
    private Integer trainNo;

    @Column(name = "arrplacecode", nullable = false, length = 10)
    private String arrPlaceCode;

    @Column(name = "arrplacename", nullable = false, length = 50)
    private String arrPlaceName;

    @Column(name = "depplacecode", nullable = false, length = 10)
    private String depPlaceCode;

    @Column(name = "depplacename", nullable = false, length = 50)
    private String depPlaceName;

    @Column(name = "arrplandtime")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime arrPlanDtime;

    @Column(name = "depplandtime")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime depPlanDtime;

    @Column(name = "adultcharge")
    private int adultCharge;

    @Column(name = "date")
    private LocalDate date;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats;
}
