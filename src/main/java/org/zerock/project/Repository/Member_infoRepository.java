package org.zerock.project.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.project.Entity.Member_info;

import java.util.Optional;

public interface Member_infoRepository extends JpaRepository<Member_info, String> {

    Optional<Member_info> findByUserNameAndPhoneNumber(String userName, String phoneNumber);
    Optional<Member_info> findByUserIdAndUserName(String userId, String userName);

    // userId 중복 체크
    boolean existsByUserId(String userId);

    // userId로 회원 조회
    Optional<Member_info> findByUserId(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE Member_info m SET m.userPw = :newPW WHERE m.userId = :userId")
    int updateUserPw(@Param("userId") String userId, @Param("newPW") String newPW);

}

