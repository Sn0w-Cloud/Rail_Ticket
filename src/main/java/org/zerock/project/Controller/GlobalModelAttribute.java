package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.zerock.project.Repository.Member_infoRepository;
import org.springframework.ui.Model;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttribute {

    private final Member_infoRepository member_infoRepository;

    @ModelAttribute
    public void addLoginMember(HttpSession session, Model model) {

        String userId = (String) session.getAttribute("loginUser");

        if (userId != null) {
            member_infoRepository.findByUserId(userId)
                    .ifPresent(member -> model.addAttribute("loginMember", member));
        }
    }
}
