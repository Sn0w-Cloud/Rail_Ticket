package org.zerock.project.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Entity.Member_info;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final Member_infoRepository memberInfoRepository;

    public void updateMyInfo(
            String userId,
            String userEmail,
            String phoneNumber,
            String userAddress
    ) {

        Member_info member = memberInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

        member.setUserEmail(userEmail);
        member.setPhoneNumber(phoneNumber);
        member.setUserAddress(userAddress);
    }
}
