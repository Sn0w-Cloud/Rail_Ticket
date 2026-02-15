package org.zerock.project.Service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Seat;
import org.zerock.project.Repository.CartRepository;
import org.zerock.project.Repository.SeatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final SeatsRepository seatsRepository;

    @Transactional
    public List<Cart_info> getCartByMember(Member_info member) {
        // 1. 현재 사용자의 모든 장바구니 아이템 조회
        List<Cart_info> cartList = cartRepository.findByMemberInfo(member);

        LocalDateTime limit = LocalDateTime.now().minusMinutes(5);

        List<Cart_info> toDelete = cartList.stream()
                .filter(cart -> cart.getSeat().getReservedAt() != null &&
                        cart.getSeat().getReservedAt().isBefore(limit))
                .collect(Collectors.toList());

        for (Cart_info cart : toDelete) {
            Seat seat = cart.getSeat();
            seat.setStatus("AVAILABLE");
            seat.setReservedAt(null);
            seatsRepository.save(seat);

            cartRepository.delete(cart);
            cartList.removeAll(toDelete);
        }

        return cartList;
    }

    @Transactional
    public Cart_info addSeatToCart(Integer seatId, Member_info member) {
        // 1️⃣ 좌석 조회 + 비관적 락 (동시 선택 방지)
        Seat seat = seatsRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new IllegalStateException("좌석이 존재하지 않습니다"));

        // 2️⃣ 만료된 예약 해제 (기존 좌석이 5분 넘게 점유 중이면 해제)
        if ("RESERVED".equals(seat.getStatus()) &&
                seat.getReservedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {

            // 기존 점유자의 장바구니 데이터 삭제 (이 부분이 추가되어야 깨끗합니다)
            cartRepository.deleteBySeat(seat);

            seat.setStatus("AVAILABLE");
            seat.setReservedAt(null);
        }

        // 3️⃣ 상태 확인 후 장바구니 담기
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new IllegalStateException("이미 다른 사용자가 선택 중인 좌석입니다");
        }

        // 4️⃣ 신규 점유 설정
        seat.setStatus("RESERVED");
        seat.setReservedAt(LocalDateTime.now());

        Cart_info cart = new Cart_info();
        cart.setMemberInfo(member);
        cart.setSeat(seat);
        cart.setTrainInfo(seat.getTrain());
        cart.setTotalPrice(seat.getTrain().getAdultCharge());

        // 저장된 객체를 리턴합니다.
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Optional<Cart_info> getCartById(Long cartId) {
        return cartRepository.findByIdWithDetails(cartId);
    }

    @Transactional(readOnly = true)
    public Optional<Cart_info> getCartByPayKey(String merchantPayKey) {
        return cartRepository.findByMerchantPayKey(merchantPayKey);
    }

    @Transactional
    public void cancelCart(Long cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            // 1. 좌석 상태 복구
            Seat seat = cart.getSeat();
            seat.setStatus("AVAILABLE");
            seat.setReservedAt(null);
            seatsRepository.save(seat);

            // 2. 장바구니 데이터 삭제
            cartRepository.delete(cart);
        });
    }

}
