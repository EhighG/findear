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

            log.info("분실물 매칭 service");
            // request dto 생성
            MatchingFindearDatasToAiReqDto matchingFindearDatasToAiReqDto = MatchingFindearDatasToAiReqDto
                    .builder().lostBoard(lostBoardMatchingDto).acquiredBoardList(new ArrayList<>()).build();

            log.info("request dto 생성 완료");

            LocalDate dateTime = LocalDate.parse(lostBoardMatchingDto.getLostAt(), DateTimeFormatter.ISO_DATE);

            // 카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송
            List<AcquiredBoard> acquiredBoardList = acquiredBoardRepository
                    .findAllWithBoardByCategoryAndAfterLostAt(lostBoardMatchingDto.getCategoryName(), dateTime.atStartOfDay());

            log.info("카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송");
            for(AcquiredBoard ab : acquiredBoardList) {

                matchingFindearDatasToAiReqDto.getAcquiredBoardList()
                        .add(AcquiredBoardMatchingDto.builder()
                                .acquiredBoardId(ab.getBoard().getId().toString())
                                .productName(ab.getBoard().getProductName())
                                .color(ab.getBoard().getColor())
                                .categoryName(ab.getBoard().getCategoryName())
                                .description(ab.getBoard().getAiDescription())
                                .xPos(ab.getXPos().toString())
                                .yPos(ab.getYPos().toString())
                                .registeredAt(ab.getBoard().getRegisteredAt().toString())
                                .build());
            }

            log.info("들어온 데이터 : " + matchingFindearDatasToAiReqDto.toString());

            log.info("ai 서버로 요청");
            // ai 서버로 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            HttpEntity<?> requestEntity = new HttpEntity<>(matchingFindearDatasToAiReqDto, headers);

            String serverURL = "https://j10a706.p.ssafy.io/match/matching/findear";

            RestTemplate restTemplate = new RestTemplate();

            log.info("요청된 데이터 : " + requestEntity.getBody());
            ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

            List<Map<String, Object>> resultList = (List<Map<String, Object>> ) response.getBody().get("result");

            log.info("매칭 결과 : " + response.getBody());

            List<MatchingFindearDatasToAiResDto> result = new ArrayList<>();

            for(Map<String, Object> res : resultList) {

                MatchingFindearDatasToAiResDto matchingFindearDatasToAiResDto = MatchingFindearDatasToAiResDto.builder()
                        .lostBoardId(res.get("lostBoardId"))
                        .acquiredBoardId(res.get("acquiredBoardId"))
                        .similarityRate(res.get("similarityRate")).build();

                result.add(matchingFindearDatasToAiResDto);
            }

            return result;

        } catch (Exception e) {

            throw new FindearException(e.getMessage());
        }
    }

}
