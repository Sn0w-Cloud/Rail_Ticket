package org.zerock.project.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Repository.Member_infoRepository;


import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final Member_infoRepository memberRepository;

    // 아이디 중복 체크

    // 회원가입
    @Transactional
    public void signup(String userId,
                       String userPw,
                       String userName,
                       String userAddress,
                       String userEmail,
                       String phoneNumber,
                       String birthDate) {

        // 1. 이미 존재하면 예외
        if (memberRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        // 2. Member 객체 생성
        Member_info member = new Member_info();
        member.setUserId(userId);
        member.setUserPw(userPw);
        member.setUserName(userName);
        member.setUserAddress(userAddress);
        member.setUserEmail(userEmail);
        member.setPhoneNumber(phoneNumber);

        if (birthDate != null && !birthDate.isEmpty()) {
            member.setBirthDate(LocalDate.parse(birthDate));
        }

        member.setMemberType(Member_info.MemberType.user);

        // 3. 저장
        memberRepository.save(member);
    }

    public LoginResult login(String userId, String password) {
        Optional<Member_info> memberOptional = memberRepository.findByUserId(userId);

        if (memberOptional.isEmpty()) {
            return LoginResult.ID_NOT_FOUND;
        }

        Member_info member = memberOptional.get();

        if (member.getStatus() == Member_info.MemberStatus.deleted) {
            return LoginResult.DELETED;
        }

        if (!member.getUserPw().equals(password)) {
            return LoginResult.PASSWORD_INCORRECT;
        }

        return LoginResult.SUCCESS;
    }

    // =================== 아이디 찾기 ===================
    public String findUserId(String userName, String phoneNumber) {
        Member_info member = memberRepository
                .findByUserNameAndPhoneNumber(userName, phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 이름으로 된 아이디가 존재하지 않습니다."));

        return member.getUserId();
    }

    // =================== 비밀번호 찾기 ===================
    public String findUserPW(String userId, String userName) {
        Member_info member = memberRepository
                .findByUserIdAndUserName(userId, userName)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 아이디로 된 정보가 존재하지 않습니다."));

        return member.getUserId();
    }

    // =================== 비밀번호 변경 ===================
    @Transactional
    public boolean changePw(String userId, String newPW) {
        int updated = memberRepository.updateUserPw(userId, newPW);
        return updated > 0; // 1 이상이면 성공
    }

    public boolean isUserIdExists(String userId) {
        if(userId == null) return false;             // null 처리
        String cleanedId = userId.trim().toLowerCase(); // 공백 제거 + 소문자 처리
        return memberRepository.existsByUserId(cleanedId);
    }

    public Member_info findByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
    }

    public Member_info getMemberById(String userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음: " + userId));
    }

    public boolean withdraw(String userId) {
        Optional<Member_info> optionalMember = memberRepository.findByUserId(userId);
        if (optionalMember.isPresent()) {
            Member_info member = optionalMember.get();
            member.setStatus(Member_info.MemberStatus.deleted);
            memberRepository.save(member);
            return true;
        }
        return false;
    }


}