package org.zerock.project.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zerock.project.Service.LoginResult;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final Member_infoRepository member_infoRepository; // ⭐ 필드 선언

    // =================== 공개 메인 페이지 ===================
    @GetMapping("/")
    public String homepage(HttpSession session, Model model) {

        String userId = (String) session.getAttribute("loginUser");

        if (userId != null) {
            Member_info member = member_infoRepository.findByUserId(userId)
                    .orElse(null);
            model.addAttribute("loginMember", member); // ⭐ 객체는 따로
        }

        return "main";
    }


    // =================== 로그인 페이지 ===================
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/member/"; // 이미 로그인 되어있으면 메인으로
        }
        return "login";
    }

    // =================== 로그인 처리 ===================
    @PostMapping("/login")
    public String loginProcess(@RequestParam String userId,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        LoginResult result = memberService.login(userId, password);

        switch(result) {
            case SUCCESS:
                session.setAttribute("loginUser", userId); // 세션에 로그인 정보 저장
                return "redirect:/member/"; // 로그인 성공 시 main.html
            case ID_NOT_FOUND:
                model.addAttribute("error", "아이디를 잘못 입력하셨습니다!");
                break;
            case PASSWORD_INCORRECT:
                model.addAttribute("error", "비밀번호를 잘못 입력하셨습니다!");
                break;
            case DELETED:
                model.addAttribute("error", "탈퇴된 회원입니다. 로그인할 수 없습니다.");
                break;
        }

        return "login"; // 로그인 실패 시 다시 login.html
    }


    // =================== 회원가입 페이지 ===================
    @GetMapping("/signup")
    public String signupPage(HttpSession session) {
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/member/"; // 로그인 되어있으면 메인으로
        }
        return "signup";
    }

    // =================== 회원가입 처리 ===================
    @PostMapping("/signup")
    public String signupProcess(@RequestParam String userId,
                                @RequestParam String userPw,
                                @RequestParam String userName,
                                @RequestParam String userAddress,
                                @RequestParam String userEmail,
                                @RequestParam String phoneNumber,
                                @RequestParam(required = false) String birthDate,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        // 빈값 체크
        if (userId.isEmpty() || userPw.isEmpty() || userName.isEmpty() ||
                userAddress.isEmpty() || userEmail.isEmpty() || phoneNumber.isEmpty()) {
            model.addAttribute("error", "모든 필드를 입력해주세요!");
            return "signup";
        }

        try {
            // 회원가입 시도
            memberService.signup(userId, userPw, userName, userAddress, userEmail, phoneNumber, birthDate);

            // 성공 시 메시지
            redirectAttributes.addFlashAttribute("message", "회원가입 성공! 로그인해주세요.");
            return "redirect:/member/login";

        } catch (IllegalStateException e) {
            // 아이디 중복 등 예외 처리
            model.addAttribute("error", e.getMessage());
            return "signup";
        } catch (Exception e) {
            // 기타 DB 오류
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "signup";
        }
    }

    @GetMapping("/check-id")
    @ResponseBody
    public String checkUserId(@RequestParam String userId) {
        boolean exists = memberService.isUserIdExists(userId);
        return exists ? "EXIST" : "AVAILABLE";
    }


    // =================== 로그아웃 ===================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 제거 → 로그인 상태 해제
        return "redirect:/member/"; // 메인으로 이동
    }


    @GetMapping("/Find_account")
    public String findIdForm() {
        return "Find_account";
    }

    @GetMapping("/Change_Pw")
    public String ChangePwForm() {
        return "Change_Pw";
    }

    @PostMapping("/SearchID")
    public String findId(@RequestParam String userName,
                         @RequestParam String phoneNumber,
                         Model model) {
        try {
            String userId = memberService.findUserId(userName, phoneNumber);
            model.addAttribute("userId", userId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "Find_account";
    }

    @PostMapping("/SearchPW")
    public String findPW(@RequestParam String userId,
                         @RequestParam String userName,
                         Model model, RedirectAttributes redirectAttributes) {
        try {
            String userPw = memberService.findUserPW(userId, userName);
            redirectAttributes.addFlashAttribute("userId", userId);
            return "redirect:/member/Change_Pw";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "Find_account";
        }
    }

    @PostMapping("/Change_Pw")
    public String changePw(@RequestParam String userId,
                           @RequestParam String newPW,
                           Model model) {

        boolean success = memberService.changePw(userId, newPW);

        if (success) {
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
            model.addAttribute("messageType", "success");
        } else {
            model.addAttribute("message", "비밀번호 변경에 실패했습니다.");
            model.addAttribute("messageType", "error");
        }
        return "Change_Pw";
    }

    @ControllerAdvice
    public class GlobalControllerAdvice {
        @ModelAttribute("loginUser")
        public Object loginUser(HttpSession session) {
            return session.getAttribute("loginUser");
        }
    }
}
