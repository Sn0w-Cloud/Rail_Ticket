package org.zerock.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.project.Entity.Train_info;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TrainInfoRepository
        extends JpaRepository<Train_info, Long> {
    boolean existsByTrainNoAndDepPlaceNameAndArrPlaceNameAndDate(Integer trainNo, String depPlaceName, String arrPlaceName, LocalDate date);

    Train_info findByTrainNoAndDepPlaceNameAndArrPlaceNameAndDate(Integer trainNo, String depPlaceName, String arrPlaceName, LocalDate date);

}
