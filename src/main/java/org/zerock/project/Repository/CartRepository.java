package org.zerock.project.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Seat;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart_info, Long> {

    @Modifying
    @Transactional
    void deleteBySeat(Seat seat);

    @Query("SELECT c FROM Cart_info c " +
            "JOIN FETCH c.seat s " +
            "JOIN FETCH s.train t " +
            "WHERE c.cartId = :cartId")
    Optional<Cart_info> findByIdWithDetails(@Param("cartId") Long cartId);

    List<Cart_info> findByMemberInfo(Member_info member);

    Optional<Cart_info> findByMerchantPayKey(String merchantPayKey);

}
