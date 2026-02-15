package org.zerock.project.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.project.Entity.Seat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatsRepository extends JpaRepository<Seat, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.seatId = :seatId")
    Optional<Seat> findByIdForUpdate(@Param("seatId") Integer seatId);

    boolean existsByTrain_TrnId(Long trnId);

    List<Seat> findByTrain_TrnIdOrderBySeatNumberAsc(Long trnId);

    List<Seat> findByTrain_TrnIdAndStatusAndReservedAtBefore(Long trnId, String status, LocalDateTime time);

}
