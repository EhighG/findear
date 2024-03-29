package com.findear.batch.ours.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findear.batch.common.exception.FindearException;
import com.findear.batch.ours.domain.AcquiredBoard;
import com.findear.batch.ours.domain.LostBoard;
import com.findear.batch.ours.dto.AcquiredBoardMatchingDto;
import com.findear.batch.ours.dto.LostBoardMatchingDto;
import com.findear.batch.ours.dto.MatchingFindearDatasToAiReqDto;
import com.findear.batch.ours.dto.MatchingFindearDatasToAiResDto;
import com.findear.batch.ours.repository.AcquiredBoardRepository;
import com.findear.batch.ours.repository.FindearMatchingLogRepository;
import com.findear.batch.ours.repository.LostBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FindearDataService {

    private final FindearMatchingLogRepository findearMatchingLogRepository;
    private final LostBoardRepository lostBoardRepository;
    private final AcquiredBoardRepository acquiredBoardRepository;

    public List<MatchingFindearDatasToAiResDto> matchingFindearDatasBatch() {

        try {

            // 찾아지지 않은 분실물 게시글 모두 조회
            List<LostBoard> lostBoardList = lostBoardRepository.findAllWithBoardByStatusOngoing();

            for(LostBoard l : lostBoardList) {

                // 분실물 게시글 정보
                LostBoardMatchingDto lostBoardMatchingDto = LostBoardMatchingDto.builder()
                        .lostBoardId(l.getId().toString())
                        .productName(l.getBoard().getProductName())
                        .color(l.getBoard().getColor())
                        .categoryName(l.getBoard().getCategoryName())
                        .description(l.getBoard().getAiDescription())
                        .lostAt(l.getLostAt().toString())
                        .xPos(l.getXPos().toString())
                        .yPos(l.getYPos().toString()).build();

                LocalDate dateTime = LocalDate.parse(lostBoardMatchingDto.getLostAt(), DateTimeFormatter.ISO_DATE);

                // 카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송
                List<AcquiredBoard> acquiredBoardList = acquiredBoardRepository
                        .findAllWithBoardByCategoryAndAfterLostAt(lostBoardMatchingDto.getCategoryName(),
                                dateTime.atStartOfDay());

                // request dto 생성
                MatchingFindearDatasToAiReqDto matchingFindearDatasToAiReqDto = MatchingFindearDatasToAiReqDto
                        .builder().lostBoard(lostBoardMatchingDto).acquiredBoardList(new ArrayList<>()).build();

                for(AcquiredBoard ab : acquiredBoardList) {
                    matchingFindearDatasToAiReqDto.getAcquiredBoardList()
                            .add(AcquiredBoardMatchingDto.builder()
                                    .acquiredBoardId(ab.getId().toString())
                                    .productName(ab.getBoard().getProductName())
                                    .color(ab.getBoard().getColor())
                                    .categoryName(ab.getBoard().getCategoryName())
                                    .description(ab.getBoard().getAiDescription())
                                    .xPos(ab.getXPos().toString())
                                    .yPos(ab.getYPos().toString())
                                    .registeredAt(ab.getBoard().getRegisteredAt().toString())
                                    .build());
                }

                // ai 서버로 요청
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

                HttpEntity<?> requestEntity = new HttpEntity<>(matchingFindearDatasToAiReqDto, headers);

                String serverURL = "https://j10a706.p.ssafy.io/match/matching/findear";

                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

                System.out.println("response : " + response.getBody());

            }

            return null;

        } catch (Exception e) {
            throw new FindearException(e.getMessage());
        }
    }
    public List<MatchingFindearDatasToAiResDto> matchingFindearDatas(LostBoardMatchingDto lostBoardMatchingDto) {

        try {

            System.out.println("분실물 데이터:");
            System.out.println("xpos : " + lostBoardMatchingDto.getXPos());
            System.out.println("ypos : " + lostBoardMatchingDto.getYPos());

            log.info("분실물 매칭 service");
            // request dto 생성
            MatchingFindearDatasToAiReqDto matchingFindearDatasToAiReqDto = MatchingFindearDatasToAiReqDto
                    .builder().lostBoard(lostBoardMatchingDto).acquiredBoardList(new ArrayList<>()).build();

            log.info("request dto 생성 완료");
//            LocalDateTime lostDate = lostBoardMatchingDto.getLostAt().atStartOfDay();

            LocalDate dateTime = LocalDate.parse(lostBoardMatchingDto.getLostAt(), DateTimeFormatter.ISO_DATE);

            // 카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송
            List<AcquiredBoard> acquiredBoardList = acquiredBoardRepository
                    .findAllWithBoardByCategoryAndAfterLostAt(lostBoardMatchingDto.getCategoryName(), dateTime.atStartOfDay());

            log.info("카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송");
            for(AcquiredBoard ab : acquiredBoardList) {

                System.out.println("습득물 데이터 : ");
                System.out.println(ab.getBoard().getId());
                System.out.println(ab.getBoard().getColor());
                System.out.println(ab.getBoard().getProductName());
                System.out.println(ab.getBoard().getCategoryName());
                System.out.println(ab.getBoard().getAiDescription());
                System.out.println(ab.getBoard().getRegisteredAt());
                System.out.println(ab.getXPos());
                System.out.println(ab.getYPos());

                matchingFindearDatasToAiReqDto.getAcquiredBoardList()
                        .add(AcquiredBoardMatchingDto.builder()
                                .acquiredBoardId(ab.getId().toString())
                                .productName(ab.getBoard().getProductName())
                                .color(ab.getBoard().getColor())
                                .categoryName(ab.getBoard().getCategoryName())
                                .description(ab.getBoard().getAiDescription())
                                .xPos(ab.getXPos().toString())
                                .yPos(ab.getYPos().toString())
                                .registeredAt(ab.getBoard().getRegisteredAt().toString())
                                .build());
            }

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("lostBoard", matchingFindearDatasToAiReqDto.getLostBoard());
            requestMap.put("acquiredBoardList", matchingFindearDatasToAiReqDto.getAcquiredBoardList());


//            // RestTemplate 객체 생성
//            RestTemplate restTemplate = new RestTemplate();
//
//// HTTP 요청 설정: DTO 객체를 직렬화하여 요청을 보냄
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                    "https://j10a706.p.ssafy.io/match/matching/findear",
//                    requestMap,
//                    String.class);
//
//            log.info("url 요청");
//
//// HTTP POST 요청에 대한 응답 확인
//            System.out.println("status : " + response.getStatusCode());
//            System.out.println("body : " + response.getBody());


            log.info("ai 서버로 요청");
            // ai 서버로 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            HttpEntity<?> requestEntity = new HttpEntity<>(requestMap, headers);

            System.out.println("요청 데이터 : " + requestEntity.getBody());
            String serverURL = "https://j10a706.p.ssafy.io/match/matching/findear";

            System.out.println("url 요청 성공");
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

            System.out.println("body : " + response.getBody().get("result"));

            List<Map<String, Object>> resultList = (List<Map<String, Object>> ) response.getBody().get("result");

            List<MatchingFindearDatasToAiResDto> result = new ArrayList<>();

            for(Map<String, Object> res : resultList) {

                System.out.println("lostBoardId : " + res.get("lostBoardId"));
                System.out.println("acquiredBoardId : " + res.get("acquiredBoardId"));
                System.out.println("simulerityRate : " + res.get("simulerityRate"));

                MatchingFindearDatasToAiResDto matchingFindearDatasToAiResDto = MatchingFindearDatasToAiResDto.builder()
                        .lostBoardId(res.get("lostBoardId"))
                        .acquiredBoardId(res.get("acquiredBoardId"))
                        .simulerityRate(res.get("simulerityRate")).build();

                result.add(matchingFindearDatasToAiResDto);
            }

            return result;
//            return null;

        } catch (Exception e) {

            throw new FindearException(e.getMessage());
        }
    }

}
