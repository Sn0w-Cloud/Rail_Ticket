package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.project.Entity.Cart_info;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.Seat;
import org.zerock.project.Entity.Train_info;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Service.CartService;
import org.zerock.project.Service.MemberService;
import org.zerock.project.Service.SeatService;
import org.zerock.project.Service.TrainService;
import org.zerock.project.dto.CartRequest;
import org.zerock.project.dto.TrainDTO;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TrainService trainService;
    private final SeatService seatService;
    private final CartService cartService;
    private final MemberService  memberService;
    @Autowired
    private Member_infoRepository member_infoRepository;

    public TicketController(TrainService trainService, SeatService seatService, CartService cartService, MemberService memberService) {
        this.trainService = trainService;
        this.seatService = seatService;
        this.cartService = cartService;
        this.memberService = memberService;
    }

    // 검색 폼에서 GET 요청 처리
    @GetMapping("/search")
    public String getTrainInfo(Model model,
                               @RequestParam String departure,
                               @RequestParam String arrival,
                               @RequestParam String date) {
        String formattedDate = date.replace("-", "");
        List<TrainDTO> trainList = trainService.getTrainInfo(departure, arrival, formattedDate);
        model.addAttribute("trainList", trainList);
        model.addAttribute("departure", departure);
        model.addAttribute("arrival", arrival);
        model.addAttribute("date", date);
        return "ticket";
    }


    @GetMapping({"", "/"})
    public String ticketPage(HttpSession session, Model model) {
        // 로그인 상태 확인
        Object loginUser = session.getAttribute("loginUser");
        model.addAttribute("loginUser", loginUser);

        return "ticket"; // templates/ticket.html
    }

    @PostMapping("/save-train")
    @ResponseBody
    public Map<String, Object> saveTrain(@RequestBody Train_info trainInfo) {
        Map<String, Object> result = new HashMap<>();
        System.out.println(trainInfo);
        try {
            Long trainId = trainService.saveTrain(trainInfo);
            String departure = trainInfo.getDepPlaceName();
            String arrival = trainInfo.getArrPlaceName();
            result.put("success", true);
            result.put("trainId", trainId);
            result.put("departure", departure);
            result.put("arrival", arrival);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/seat")
    public String seatPage(
            @RequestParam Long trainId,
            @RequestParam String departure,
            @RequestParam String arrival,
            Model model
    ) {
        List<Seat> seatList = seatService.getSeatsByTrainId(trainId);

        // 차량별 좌석 분류 (carNum이 seatNumber 첫 글자 숫자라고 가정)
        Map<Integer, List<Seat>> carSeats = seatList.stream()
                .collect(Collectors.groupingBy(s -> Integer.parseInt(s.getSeatNumber().substring(0, 1))));

        Map<String, List<Map<String, String>>> seatMap = new HashMap<>();

        for (Map.Entry<Integer, List<Seat>> entry : carSeats.entrySet()) {
            int carNum = entry.getKey();
            List<Seat> seats = entry.getValue();

            // 상단 좌석 A~E
            List<Map<String, String>> upperSeats = seats.stream()
                    .filter(s -> s.getSeatNumber().matches("\\d[A-E]"))
                    .sorted(Comparator.comparing(Seat::getSeatNumber))
                    .map(s -> Map.of(
                            "seatId", String.valueOf(s.getSeatId()),
                            "seatNumber", s.getSeatNumber(),
                            "status", s.getStatus()
                    ))
                    .toList();

            // 하단 좌석 F~J
            List<Map<String, String>> lowerSeats = seats.stream()
                    .filter(s -> s.getSeatNumber().matches("\\d[F-J]"))
                    .sorted(Comparator.comparing(Seat::getSeatNumber))
                    .map(s -> Map.of(
                            "seatId", String.valueOf(s.getSeatId()),
                            "seatNumber", s.getSeatNumber(),
                            "status", s.getStatus()
                    ))
                    .toList();

            seatMap.put("car" + carNum + "_upper", upperSeats);
            seatMap.put("car" + carNum + "_lower", lowerSeats);
        }

        model.addAttribute("trainId", trainId);
        model.addAttribute("departure", departure);
        model.addAttribute("arrival", arrival);
        model.addAttribute("cars", carSeats.keySet());
        model.addAttribute("seatMap", seatMap);

        return "seat"; // seat.html
    }

    @PostMapping("/cart")
    @ResponseBody
    public ResponseEntity<Void> addToCart(
            @RequestBody CartRequest request,
            HttpSession session
    ) {
        Object loginUser = session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        String userId = loginUser.toString();

        Member_info member = memberService.findByUserId(userId);

        cartService.addSeatToCart(request.getSeatId(), member);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/direct-purchase")
    @ResponseBody
    public ResponseEntity<?> directPurchase(@RequestBody Map<String, Integer> request, HttpSession session) {

        // 세션에서 꺼낸 값이 String일 경우를 대비해 Object로 먼저 받습니다.
        Object userObj = session.getAttribute("loginUser");

        // 만약 세션에 유저 객체가 아닌 유저 ID(String)만 들어있다면, DB에서 유저를 조회해야 합니다.
        Member_info loginUser;
        if (userObj instanceof String) {
            String userId = (String) userObj;
            loginUser = member_infoRepository.findById(userId) // 또는 findByName 등 프로젝트에 맞는 메서드
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        } else {
            loginUser = (Member_info) userObj;
        }

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Integer seatId = request.get("seatId");
        Cart_info cart = cartService.addSeatToCart(seatId, loginUser);

        return ResponseEntity.ok(Map.of("cartId", cart.getCartId()));
    }
}

