package org.zerock.project.Service;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Entity.Purchase_list;
import org.zerock.project.Entity.Seat;
import org.zerock.project.Repository.CartRepository;
import org.springframework.http.HttpHeaders;
import org.zerock.project.Repository.PurchaseRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseFacadeService {

    private final CartService cartService;
    private final SeatService seatService;
    private final PurchaseService purchaseService;
    private final CartRepository cartRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public void purchase(String merchantPayKey) {

        Cart_info cart = cartService.getCartByPayKey(merchantPayKey)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Seat seat = cart.getSeat();

        if (!"RESERVED".equals(seat.getStatus())) {
            throw new IllegalStateException("좌석 상태가 올바르지 않습니다");
        }

        seat.setStatus("BOOKED");
        seat.setReservedAt(null);
        seatService.save(seat);

        purchaseService.addPurchase(cart);

        cartRepository.delete(cart);
    }

    @Transactional
    public void cancelPurchase(Long listId) {
        Purchase_list purchase = purchaseRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("해당 구매 내역이 없습니다."));

        boolean isCancelled = purchaseService.cancelNaverPayment(
                purchase.getPaymentId(),
                purchase.getPrice()
        );

        if (isCancelled) {
            // 2. API 성공 시에만 DB 상태 변경
            purchase.setStatus(Purchase_list.PurchaseStatus.CANCELED);

            purchase.getSeat().setStatus("AVAILABLE");
        } else {
            throw new RuntimeException("네이버페이 환불 처리에 실패했습니다.");
        }
    }



}

