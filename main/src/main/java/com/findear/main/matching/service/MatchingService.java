package com.findear.main.matching.service;

import com.findear.main.board.common.domain.Board;
import com.findear.main.board.query.dto.AcquiredBoardDetailResDto;
import com.findear.main.board.query.dto.BatchServerResponseDto;
import com.findear.main.board.query.dto.LostBoardDetailResDto;
import com.findear.main.board.query.service.AcquiredBoardQueryService;
import com.findear.main.board.query.service.LostBoardQueryService;
import com.findear.main.matching.model.dto.GetBestMatchingsResDto;
import com.findear.main.matching.model.dto.BatchServerMatchingResDto;
import com.findear.main.matching.repository.MatchingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MatchingService {

    private final RestTemplate restTemplate;
    private final String BATCH_SERVER_URL = "https://j10a706.p.ssafy.io/batch/search";
    private final LostBoardQueryService lostBoardQueryService;
    private final AcquiredBoardQueryService acquiredBoardQueryService;

    public List<GetBestMatchingsResDto> getBestMatchings(Long memberId) {
        List<BatchServerMatchingResDto> matchings = requestBestMatchings(memberId);
        List<GetBestMatchingsResDto> result = new ArrayList<>(matchings.size());

        for (BatchServerMatchingResDto matchingInfo : matchings) {
            result.add(matchingToResult(matchingInfo));
        }
        return result;
    }

    private GetBestMatchingsResDto matchingToResult(BatchServerMatchingResDto matchingRes) {
        LostBoardDetailResDto lostBoardDto = lostBoardQueryService.findById(matchingRes.getLostBoardId());
        AcquiredBoardDetailResDto acquiredBoardDto = acquiredBoardQueryService.findById(matchingRes.getAcquiredBoardId());

        return GetBestMatchingsResDto.builder()
                .lostBoardDto(lostBoardDto)
                .acquiredBoardDto(acquiredBoardDto)
                .similarityRate(matchingRes.getSimilarityRate())
                .matchedAt(matchingRes.getMatchedAt())
                .build();
    }

    private List<BatchServerMatchingResDto> requestBestMatchings(Long memberId) {
        String url = BATCH_SERVER_URL + "?memberId=" + memberId;
        BatchServerResponseDto response = restTemplate.getForObject(url, BatchServerResponseDto.class);
        return (List<BatchServerMatchingResDto>) response.getResult();
    }
}
