package org.zerock.project.Repository;

import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Purchase_list;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase_list, Long> {

    List<Purchase_list> findByMemberInfoAndStatusAndTrainInfo_DateLessThanEqual(
            Member_info member, Purchase_list.PurchaseStatus status, LocalDate date);

    List<Purchase_list> findByMemberInfoOrderByPurchaseDateDesc(Member_info memberInfo);

}
