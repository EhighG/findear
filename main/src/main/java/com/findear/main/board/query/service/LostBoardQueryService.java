package com.findear.main.board.query.service;


import com.findear.main.board.common.domain.LostBoard;
import com.findear.main.board.query.dto.LostBoardDetailResDto;
import com.findear.main.board.query.dto.LostBoardListResDto;
import com.findear.main.board.query.dto.LostBoardListResponse;
import com.findear.main.board.query.repository.LostBoardQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardQueryService {

    private final LostBoardQueryRepository lostBoardQueryRepository;
    private final int PAGE_SIZE = 10;

    private final String DEFAULT_SDATE_STRING = "2015-01-01";

    public LostBoardListResponse findAll(Long memberId, String category, String sDate, String eDate, String keyword, int pageNo) {
        List<LostBoard> lostBoards = lostBoardQueryRepository.findAll();
        Stream<LostBoard> stream = lostBoards.stream();

        // filtering
        if (memberId != null) {
            stream = stream.filter(lost -> lost.getBoard().getMember().getId().equals(memberId));
        }
        if (category != null) {
            stream = stream.filter(lost -> lost.getBoard().getCategoryName().equals(category));
        }
        if (sDate != null || eDate != null) {
            stream = stream.filter(
                    lost -> !lost.getLostAt().isBefore(sDate != null ? LocalDate.parse(sDate) : LocalDate.parse(DEFAULT_SDATE_STRING))
                            && !lost.getLostAt().isAfter(eDate != null ? LocalDate.parse(eDate) : LocalDate.now())
            );
        }
        if (keyword != null) {
            stream = stream.filter(lost -> lost.getBoard().getProductName().contains(keyword));
        }

        List<LostBoardListResDto> filtered = stream
                .map(LostBoardListResDto::of)
                .toList();

        // paging
        int eIdx = PAGE_SIZE * pageNo;
        int sIdx = eIdx - PAGE_SIZE;
        if (sIdx >= filtered.size()) return null;
        return new LostBoardListResponse(filtered.subList(sIdx, Math.min(eIdx, filtered.size())), filtered.size() / PAGE_SIZE + 1);
    }

    public LostBoardDetailResDto findByBoardId(Long boardId) {
        LostBoard lostBoard = lostBoardQueryRepository.findByBoardId(boardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));

        return LostBoardDetailResDto.of(lostBoard);
    }

    public LostBoardDetailResDto findById(Long lostBoardId) {
        LostBoard lostBoard = lostBoardQueryRepository.findById(lostBoardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));
        
        return LostBoardDetailResDto.of(lostBoard);
    }
}
