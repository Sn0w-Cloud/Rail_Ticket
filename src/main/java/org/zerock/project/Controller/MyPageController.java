package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Purchase_list;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Service.MemberService;
import org.zerock.project.Service.MypageService;
import org.zerock.project.Service.PurchaseService;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final Member_infoRepository memberInfoRepository;
    private final PurchaseService purchaseService;
    private final MemberService memberService;

    @GetMapping("")
    public String myPage(HttpSession session, Model model) {

        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) {
            return "redirect:/member/login";
        }

        Member_info loginUser = memberInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

        model.addAttribute("loginUser", loginUser);

        List<Purchase_list> purchases = purchaseService.getPurchasesByMember(loginUser);

        model.addAttribute("purchases", purchases);

        return "mypage";
    }

    @PostMapping("/update")
    public String updateMyInfo(
            @RequestParam String userEmail,
            @RequestParam String phoneNumber,
            @RequestParam String userAddress,
            HttpSession session
    ) {

        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) {
            return "redirect:/member/login";
        }

        Member_info member = memberInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

        member.setUserEmail(userEmail);
        member.setPhoneNumber(phoneNumber);
        member.setUserAddress(userAddress);

        memberInfoRepository.save(member);

        return "redirect:/mypage";
    }

    @GetMapping("/Change_Pw")
    public String changePwForm() {
        return "Change_Pw";
    }

    @PostMapping("/withdraw")
    @ResponseBody
    public String withdraw(HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) {
            return "NOT_LOGGED_IN";
        }

        boolean success = memberService.withdraw(userId);
        if (success) {
            session.invalidate();
            return "SUCCESS";
        } else {
            return "FAIL";
        }
    }

}
