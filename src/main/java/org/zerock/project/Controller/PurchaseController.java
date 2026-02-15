package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Purchase_list;
import org.zerock.project.Service.MemberService;
import org.zerock.project.Service.PurchaseFacadeService;
import org.zerock.project.Service.PurchaseService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseFacadeService purchaseFacadeService;

    @GetMapping("/success")
    public String paymentSuccess() {
        return "success";
    }

    @GetMapping("/failure")
    public String paymentFailure() {
        return "failure";
    }

    @GetMapping("/cancel")
    public String cancelPurchase(@RequestParam("id") Long id) {
        purchaseFacadeService.cancelPurchase(id);

        return "redirect:/mypage#orders";
    }

}

