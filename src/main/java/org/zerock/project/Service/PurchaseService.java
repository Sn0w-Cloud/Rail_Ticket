package org.zerock.project.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.zerock.project.Entity.*;
import org.zerock.project.Repository.PurchaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.pay-chain-id}")
    private String chainId;

    @Transactional // 데이터 수정을 위해 반드시 필요
    public List<Purchase_list> getPurchasesByMember(Member_info member) {
        // 1. 오늘 날짜 가져오기
        LocalDate today = LocalDate.now();

        // 2. 이 회원의 데이터 중 'AVAILABLE'이면서 출발일이 오늘이거나 지난 데이터 조회
        // (trnDate 필드명은 프로젝트에 맞게 수정하세요)
        List<Purchase_list> expiredItems = purchaseRepository
                .findByMemberInfoAndStatusAndTrainInfo_DateLessThanEqual(member, Purchase_list.PurchaseStatus.AVAILABLE, today);

        // 3. 해당 데이터들의 상태를 'COMPLETED'로 변경 (Dirty Checking으로 자동 업데이트됨)
        for (Purchase_list item : expiredItems) {
            item.setStatus(Purchase_list.PurchaseStatus.COMPLETED);
        }

        // 4. 업데이트된 내용을 포함하여 전체 리스트 반환
        return purchaseRepository.findByMemberInfoOrderByPurchaseDateDesc(member);
    }

    public void addPurchase(Cart_info cart) {
        Purchase_list purchase = new Purchase_list();
        Train_info train = cart.getTrainInfo();
        Seat seat = cart.getSeat();

        String listName =
                train.getDate()
                        + " "
                        +
                        train.getDepPlaceName()
                        + " → "
                        + train.getArrPlaceName()
                        + " ("
                        + train.getDepPlanDtime()
                        + " ~ "
                        + train.getArrPlanDtime()
                        + ")"
                        + " 좌석: "
                        + seat.getSeatNumber();

        purchase.setListName(listName);

        purchase.setMemberInfo(cart.getMemberInfo());
        purchase.setSeat(cart.getSeat());
        purchase.setTrainInfo(cart.getTrainInfo());
        purchase.setPrice(cart.getTotalPrice());
        purchase.setMerchantPayKey(cart.getMerchantPayKey());
        purchase.setPaymentId(cart.getPaymentId());
        purchase.setPurchaseDate(LocalDateTime.now());

        purchaseRepository.save(purchase);
    }

    public boolean cancelNaverPayment(String paymentId, int amount) {
        String url = "https://dev-pay.paygate.naver.com/naverpay-partner/naverpay/payments/v1/cancel";

        RestTemplate restTemplate = new RestTemplate();

        // 1. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.set("X-NaverPay-Chain-Id", chainId);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 2. 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("paymentId", paymentId);
        params.add("cancelAmount", String.valueOf(amount));
        params.add("taxScopeAmount", String.valueOf(amount));
        params.add("cancelReason", "사용자 변심 및 취소");
        params.add("cancelRequester", "1"); // 1: 구매자, 2: 가맹점 관리자

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            System.out.println("[NaverPay Cancel Response] " + body);
            // 네이버페이 응답 코드 'Success' 확인
            return body != null && "Success".equals(body.get("code"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
