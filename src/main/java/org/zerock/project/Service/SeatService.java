package org.zerock.project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.project.Entity.Seat;
import org.zerock.project.Entity.Train_info;
import org.zerock.project.Repository.SeatsRepository;
import org.zerock.project.Repository.TrainInfoRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class SeatService {

    private final SeatsRepository seatRepository;
    private final TrainInfoRepository trainInfoRepository;

    public SeatService(SeatsRepository seatRepository, TrainInfoRepository trainInfoRepository) {
        this.seatRepository = seatRepository;
        this.trainInfoRepository = trainInfoRepository;
    }

    @Transactional
    public void createSeatsIfNotExists(Long trnId) {
        // 좌석이 이미 존재하면 생성하지 않음
        if (!seatRepository.existsByTrain_TrnId(trnId)) {

            Train_info train = trainInfoRepository.findById(trnId)
                    .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trnId));

            String[] seatLetters = {"A","B","C","D","E","F","G","H","I","J"};

            for (int row = 1; row <= 2; row++) {          // 1행, 2행
                for (String letter : seatLetters) {       // A ~ J
                    Seat seat = new Seat();
                    seat.setTrain(train);
                    seat.setSeatNumber(row + letter);    // 1A, 1B ... 2J
                    seat.setStatus("AVAILABLE");          // 초기 상태
                    seatRepository.save(seat);
                }
            }
        }
    }

    public List<Seat> getSeatsByTrainId(Long trnId) {

        releaseExpiredReservations(trnId);

        return seatRepository.findByTrain_TrnIdOrderBySeatNumberAsc(trnId);
    }

    @Transactional
    public void releaseExpiredReservations(Long trnId) {
        LocalDateTime now = LocalDateTime.now();
        List<Seat> expiredSeats = seatRepository.findByTrain_TrnIdAndStatusAndReservedAtBefore(
                trnId, "RESERVED", now.minusMinutes(5)
        );

        for (Seat seat : expiredSeats) {
            seat.setStatus("AVAILABLE");
            seat.setReservedAt(null);
            seatRepository.save(seat);
        }
    }
    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }
}