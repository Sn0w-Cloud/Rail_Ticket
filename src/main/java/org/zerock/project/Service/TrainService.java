package org.zerock.project.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zerock.project.Entity.Train_info;
import org.zerock.project.Repository.TrainInfoRepository;
import org.zerock.project.dto.TrainDTO;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainService {
    @Autowired
    private TrainInfoRepository trainInfoRepository;
    private final SeatService seatService;

    @Value("${public-data.service-key}")
    private String serviceKey;

    private String calculateDuration(String depTimeStr, String arrTimeStr) {
        try {
            // API에서 받은 시간 형식: yyyyMMddHHmmss
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime depTime = LocalDateTime.parse(depTimeStr, formatter);
            LocalDateTime arrTime = LocalDateTime.parse(arrTimeStr, formatter);

            // 두 시간의 차이 계산
            Duration duration = Duration.between(depTime, arrTime);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;

            // HH:mm 형식으로 반환
            return String.format("%02d시간 %02d분", hours, minutes);
        } catch (Exception e) {
            // 변환 실패 시 원래 값을 반환
            return "시간 계산 오류";
        }
    }


    public List<TrainDTO> getTrainInfo(String dep, String arr, String date) {
        String url = "https://apis.data.go.kr/1613000/TrainInfoService/getStrtpntAlocFndTrainInfo"
                + "?serviceKey=" + serviceKey
                + "&depPlaceId=" + dep
                + "&arrPlaceId=" + arr
                + "&depPlandTime=" + date
                + "&trainGradeCode=00"
                + "&numOfRows=100"
                + "&pageNo=1"
                + "&_type=json";
        RestTemplate restTemplate = new RestTemplate();

        JsonNode responseJson = restTemplate.getForObject(url, JsonNode.class);

        if (responseJson == null) {
            throw new RuntimeException("API 응답이 없습니다.");
        }

        return parseTrainJson(responseJson);
    }

    private List<TrainDTO> parseTrainJson(JsonNode root) {
        List<TrainDTO> trainList = new ArrayList<>();
        try {
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode node : items) {
                    TrainDTO train = new TrainDTO();
                    train.setTrainNo(Integer.parseInt(node.path("trainno").asText()));
                    train.setTrainGradeName(node.path("traingradename").asText());
                    train.setDepPlaceName(node.path("depplacename").asText());
                    train.setDepPlanTime(formatTime(node.path("depplandtime").asText()));
                    train.setArrPlaceName(node.path("arrplacename").asText());
                    train.setArrPlanTime(formatTime(node.path("arrplandtime").asText()));
                    train.setAdultCharge(Integer.parseInt(node.path("adultcharge").asText()));
                    String duration = calculateDuration(node.path("depplandtime").asText(), node.path("arrplandtime").asText());
                    train.setDuration(duration);

                    trainList.add(train);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainList;
    }

    private String formatTime(String dateTimeStr) {
        try {
            // API에서 받은 시간 형식: yyyyMMddHHmmss
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, inputFormat);

            // 출력 형식: HH:mm
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("HH:mm");
            return dateTime.format(outputFormat);
        } catch (Exception e) {
            // 변환 실패하면 원래 값 반환
            return dateTimeStr;
        }
    }

    @Transactional
    public Long saveTrain(Train_info trainInfo) {

        LocalDate date = trainInfo.getDate();
        System.out.println(trainInfo.getTrainNo());
        if (trainInfoRepository.existsByTrainNoAndDepPlaceNameAndArrPlaceNameAndDate(
                trainInfo.getTrainNo(),
                trainInfo.getDepPlaceName(),
                trainInfo.getArrPlaceName(),
                date)) {
            return trainInfoRepository.findByTrainNoAndDepPlaceNameAndArrPlaceNameAndDate(
                    trainInfo.getTrainNo(),
                    trainInfo.getDepPlaceName(),
                    trainInfo.getArrPlaceName(),
                    date).getTrnId();
        } else {
            trainInfoRepository.save(trainInfo);
            Long trnId = trainInfo.getTrnId();
            seatService.createSeatsIfNotExists(trnId);

            return trnId;
        }
    }

}
