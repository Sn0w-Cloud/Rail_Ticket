package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Service.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")

public class CartController {
    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.pay-chain-id}")
    private String chainId;

    @Autowired
    private final CartService cartService;
    private final MemberService  memberService;

    public CartController(CartService cartService, MemberService memberService) {
        this.cartService = cartService;
        this.memberService = memberService;
    }

    @GetMapping("")
    public String cartPage(HttpSession session, Model model) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        String userId = loginUser.toString();
        Member_info member = memberService.findByUserId(userId);

        List<Cart_info> cartList = cartService.getCartByMember(member);

        List<Map<String, Object>> cartItems = cartList.stream().map(cart -> {
            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getCartId());
            map.put("trainNo", cart.getTrainInfo().getTrainNo());
            map.put("depPlaceName", cart.getTrainInfo().getDepPlaceName());
            map.put("arrPlaceName", cart.getTrainInfo().getArrPlaceName());
            map.put("depPlanTime", cart.getTrainInfo().getDepPlanDtime());
            map.put("arrPlanTime", cart.getTrainInfo().getArrPlanDtime());
            map.put("seatNumber", cart.getSeat().getSeatNumber());
            map.put("price", cart.getTotalPrice());
            map.put("formattedPrice", String.format("%,d", cart.getTotalPrice()));
            map.put("date", cart.getTrainInfo().getDate());
            return map;
        }).toList();

        model.addAttribute("cartItems", cartItems);

        int totalPrice = cartItems.stream()
                .mapToInt(item -> (int) item.get("price"))
                .sum();
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("formattedTotalPrice", String.format("%,d", totalPrice));
        return "cart";
    }

    @PostMapping("/purchase/{cartId}")
    public String purchaseCartItem(@PathVariable Long cartId) {



        return "redirect:/purchase/list";
    }

    @GetMapping("/payment/{cartId}")
    public String PaymentPage(@PathVariable Long cartId, Model model) {
        Cart_info cart = cartService.getCartById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 정보를 찾을 수 없습니다."));

        model.addAttribute("clientId", clientId);
        model.addAttribute("chainId", chainId);
        model.addAttribute("cart", cart);
        return "payment_method";
    }

    @GetMapping("/cancel/{cartId}")
    public String cancelCart(@PathVariable Long cartId) {
        cartService.cancelCart(cartId);

        // 삭제 후 다시 장바구니 목록 페이지로 새로고침
        return "redirect:/cart";
    }

}
