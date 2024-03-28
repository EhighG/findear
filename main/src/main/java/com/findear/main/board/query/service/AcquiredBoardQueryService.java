package com.findear.main.board.query.service;

import com.findear.main.board.common.domain.AcquiredBoard;
import com.findear.main.board.query.dto.*;
import com.findear.main.board.query.repository.AcquiredBoardQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AcquiredBoardQueryService {

    private final AcquiredBoardQueryRepository acquiredBoardQueryRepository;
    private final String BATCH_SERVER_URL = "http://j10a706.p.ssafy.io/batch/search";
    private final int PAGE_SIZE = 10;
    private final String DEFAULT_SDATE_STRING = "2015-01-01";

    public AcquiredBoardListResponse findAll(Long memberId, String category, String sDate, String eDate, String keyword, int pageNo) {
        List<AcquiredBoard> acquiredBoards = acquiredBoardQueryRepository.findAll();
        Stream<AcquiredBoard> stream = acquiredBoards.stream();

        // filtering
        if (memberId != null) {
            stream = stream.filter(acquired -> acquired.getBoard().getMember().getId().equals(memberId));
        }
        if (category != null) {
            stream = stream.filter(acquired -> acquired.getBoard().getCategoryName().equals(category));
        }
        if (sDate != null || eDate != null) {
            stream = stream.filter(
                    acquired -> !acquired.getAcquiredAt().isBefore(sDate != null ? LocalDate.parse(sDate) : LocalDate.parse(DEFAULT_SDATE_STRING))
                            && !acquired.getAcquiredAt().isAfter(eDate != null ? LocalDate.parse(eDate) : LocalDate.now())
            );
        }
        if (keyword != null) {
            stream = stream.filter(acquired -> acquired.getBoard().getProductName().contains(keyword)
                    || acquired.getAddress().contains(keyword)
                    || acquired.getName().contains(keyword));
        }

        List<AcquiredBoardListResDto> filtered = stream
                .map(AcquiredBoardListResDto::of)
                .toList();

        // paging
        int eIdx = PAGE_SIZE * pageNo;
        int sIdx = eIdx - PAGE_SIZE;
        if (sIdx >= filtered.size()) return null;
        return new AcquiredBoardListResponse(filtered.subList(sIdx, Math.min(eIdx, filtered.size())), filtered.size() / PAGE_SIZE + 1);
    }

    public AcquiredBoardDetailResDto findById(Long boardId) {
        AcquiredBoard acquiredBoard = acquiredBoardQueryRepository.findByBoardId(boardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));

        return AcquiredBoardDetailResDto.of(acquiredBoard);
    }

    public List<?> findAllInLost112(String category, String sDate, String eDate, String keyword, int pageNo) {
        log.info("service 메소드 들어옴");
        if (sDate != null && eDate == null) {
            eDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (sDate == null && eDate != null) {
            sDate = LocalDate.parse(eDate).minusMonths(6)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        // request to batch server
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BATCH_SERVER_URL);
//        if (categoryId != null) {
//            builder = builder.queryParam("categoryId", categoryId);
//        }
//        if (sDate != null) {
//            builder = builder.queryParam("sDate", sDate)
//                    .queryParam("eDate", eDate);
//        }
//        if (keyword != null) {
//            builder = builder.queryParam("keyword", keyword);
//        }
        builder = builder.queryParam("page", pageNo)
                .queryParam("size", PAGE_SIZE);
        log.info("조회 파라미터(쿼리스트링) 세팅 끝");

//        PoliceAcquiredDto[] lost112Res = restTemplate.getForObject(builder.toUriString(), PoliceAcquiredDto[].class);
//        List<PoliceAcquiredDto> result = Arrays.asList(lost112Res);
//        return result;
        ResponseEntity<String> response = restTemplate.getForEntity(BATCH_SERVER_URL + "/test", String.class);
        log.info(BATCH_SERVER_URL + " 로 요청 & 응답 받아옴");
        if (response.getStatusCode().is3xxRedirection()) {
            String redirectUrl = response.getHeaders()
                    .getLocation().toString();

            log.info("리다이렉트 하라는 응답 받음 / url = " + redirectUrl);

            ResponseEntity<String> redirectResponse = restTemplate.getForEntity(redirectUrl, String.class);
            log.info(redirectUrl + " 로 요청 & 응답 받아옴");

            log.info("redirectResponse.getBody() = " + redirectResponse.getBody() + "\n // 배치서버의 응답");

        } else {
            log.info("첫 요청에 redirect가 아닌, 응답 바로 받아온 경우 / response.getBody() = " + response.getBody() + "\n // 배치서버의 응답");
        }

//        try {
//            URL url = new URL(BATCH_SERVER_URL + "/test");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            conn.setRequestMethod("GET");
//            conn.setConnectTimeout(3000);
//
//            int responseCode = conn.getResponseCode();
//
//            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
//
//            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//            conn.disconnect();
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }
}
