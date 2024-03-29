package com.findear.main.board.query.service;

import com.findear.main.board.common.domain.AcquiredBoard;
import com.findear.main.board.common.domain.Lost112AcquiredBoardDto;
import com.findear.main.board.query.dto.*;
import com.findear.main.board.query.repository.AcquiredBoardQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AcquiredBoardQueryService {

    private final AcquiredBoardQueryRepository acquiredBoardQueryRepository;
    private final String BATCH_SERVER_URL = "https://j10a706.p.ssafy.io/batch/search";
    private final String DEFAULT_SDATE_STRING = "2015-01-01";
    private final RestTemplate restTemplate;

    public AcquiredBoardListResponse findAll(Long memberId, String category, String sDate, String eDate, String keyword, int pageNo,
                                             int pageSize) {
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
        int eIdx = pageSize * pageNo;
        int sIdx = eIdx - pageSize;
        if (sIdx >= filtered.size()) return null;
        return new AcquiredBoardListResponse(filtered.subList(sIdx, Math.min(eIdx, filtered.size())), filtered.size() / pageSize + 1);
    }

    public AcquiredBoardDetailResDto findById(Long boardId) {
        AcquiredBoard acquiredBoard = acquiredBoardQueryRepository.findByBoardId(boardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));

        return AcquiredBoardDetailResDto.of(acquiredBoard);
    }

    public List<Lost112AcquiredBoardDto> findAllInLost112(String category, String sDate, String eDate, String keyword, int pageNo,
                                                          int pageSize) {
        log.info("service 메소드 들어옴");
        if (sDate != null && eDate == null) {
            eDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (sDate == null && eDate != null) {
            sDate = LocalDate.parse(eDate).minusMonths(6)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        // request to batch server
//        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BATCH_SERVER_URL);
        if (category != null) {
            builder = builder.queryParam("category", category);
        }
        if (sDate != null) {
            builder = builder.queryParam("sDate", sDate)
                    .queryParam("eDate", eDate);
        }
        if (keyword != null) {
            builder = builder.queryParam("keyword", keyword);
        }
        builder = builder.queryParam("page", pageNo)
                .queryParam("size", pageSize);
        log.info("조회 파라미터(쿼리스트링) 세팅 끝");

        BatchServerResponseDto responseDto = restTemplate.getForObject(builder.toUriString(), BatchServerResponseDto.class);

        return responseDto.getResult();
    }

    public Integer getLost112TotalPageNum(int pageSize) {
        Integer totalRowNum = (Integer) restTemplate.getForObject(BATCH_SERVER_URL + "/total", ResponseEntity.class).getBody();
        return Math.max(1, totalRowNum / pageSize + (totalRowNum % pageSize == 0 ? 0 : 1));
    }

}
