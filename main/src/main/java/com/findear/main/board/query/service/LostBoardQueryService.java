package com.findear.main.board.query.service;


import com.findear.main.board.common.domain.LostBoard;
import com.findear.main.board.query.dto.LostBoardDetailResDto;
import com.findear.main.board.query.dto.LostBoardListResDto;
import com.findear.main.board.query.repository.CategoryRepository;
import com.findear.main.board.query.repository.LostBoardQueryRepository;
import com.findear.main.member.query.dto.FindMemberListResDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardQueryService {

    private final LostBoardQueryRepository lostBoardQueryRepository;
    private final CategoryRepository categoryRepository;
    private final int PAGE_SIZE = 10;

    public List<LostBoardListResDto> findAll(Long memberId, Long categoryId, String sDate, String eDate, String keyword, int pageNo) {
        List<LostBoard> lostBoards = lostBoardQueryRepository.findAll();
        Stream<LostBoard> stream = lostBoards.stream();

        // filtering
        if (memberId != null) {
            stream = stream.filter(lost -> lost.getBoard().getMember().getId().equals(memberId));
        }
        if (categoryId != null) {
            String categoryName = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("invalid board category ID"))
                    .getCategoryName();

            stream = stream.filter(lost -> lost.getBoard().getCategoryName().equals(categoryName));
        }
        if (sDate != null || eDate != null) {
            stream = stream.filter(
                    lost -> !lost.getLostAt().isBefore(sDate != null ? LocalDate.parse(sDate) : LocalDate.parse(eDate).minusMonths(6))
                            && !lost.getLostAt().isAfter(eDate != null ? LocalDate.parse(eDate) : LocalDate.now().plusMonths(6))
            );
        }
        if (keyword != null) {
            stream = stream.filter(lost -> lost.getBoard().getProductName().contains(keyword)
            || lost.getBoard().getDescription().contains(keyword));
        }

        List<LostBoardListResDto> filtered = stream
                .map(LostBoardListResDto::of)
                .toList();

        // paging
        int eIdx = PAGE_SIZE * pageNo;
        int sIdx = eIdx - PAGE_SIZE;
        if (sIdx >= filtered.size()) return null;
        return filtered.subList(sIdx, Math.min(eIdx, filtered.size()));
    }

    public LostBoardDetailResDto findByBoardId(Long boardId) {
        LostBoard lostBoard = lostBoardQueryRepository.findByBoardId(boardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));

        return LostBoardDetailResDto.of(lostBoard);
    }
}
