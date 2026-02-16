package org.zerock.project.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Repository.CartRepository;
import org.zerock.project.Service.PurchaseFacadeService;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {
    private final CartRepository cartRepository;
    private final WebClient webClient;
    private final PurchaseFacadeService purchaseFacadeService;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.pay-chain-id}")
    private String chainId;

    @PostMapping("/naver/ready")
    @ResponseBody
    public Map<String, Object> ready(@RequestBody Map<String, Long> request) {
        Long cartId = request.get("cartId");

        Cart_info cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalStateException("장바구니 없음"));

        if (!"RESERVED".equals(cart.getSeat().getStatus())) {
            throw new IllegalStateException("결제 가능한 좌석 상태가 아닙니다");
        }

        String merchantPayKey = "ORDER_" + UUID.randomUUID();

        cart.setMerchantPayKey(merchantPayKey);
        cartRepository.save(cart);

        return Map.of(
                "merchantPayKey", merchantPayKey,
                "amount", cart.getTotalPrice()
        );
    }

    @GetMapping("/naver/return")
    public String returnFromNaver(
            @RequestParam String merchantPayKey,
            @RequestParam String paymentId) {

        Cart_info cart = cartRepository
                .findByMerchantPayKey(merchantPayKey)
                .orElseThrow(() -> new IllegalStateException("주문 정보 없음"));

        cart.setPaymentId(paymentId);
        cartRepository.save(cart);

        String url = "https://dev-pay.paygate.naver.com/naverpay-partner/naverpay/payments/v2.2/apply/payment";

        try {
            // 3. WebClient를 사용해 네이버페이 승인 API 호출
            String approvalResponse = webClient.post()
                    .uri(url)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .header("X-NaverPay-Chain-Id", chainId) // 샌드박스 체인 ID 필수
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED) // 중요: JSON이 아닌 Form 방식
                    .body(org.springframework.web.reactive.function.BodyInserters.fromFormData("paymentId", paymentId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.err.println("네이버 에러 상세: " + errorBody);
                                return Mono.error(new RuntimeException("API 요청 실패"));
                            }))
                    .bodyToMono(String.class)
                    .block();

            if (approvalResponse != null && approvalResponse.toLowerCase().contains("success")) {
                purchaseFacadeService.purchase(merchantPayKey);
                return "redirect:/purchase/success";
            }
        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            return "redirect:/purchase/failure";
        }
        return "redirect:/purchase/failure";
    }
}
