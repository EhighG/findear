package com.findear.main.board.query.service;

import com.findear.main.board.common.domain.AcquiredBoard;
import com.findear.main.board.query.dto.AcquiredBoardDetailResDto;
import com.findear.main.board.query.dto.AcquiredBoardListResDto;
import com.findear.main.board.query.dto.LostBoardListResDto;
import com.findear.main.board.query.repository.AcquiredBoardQueryRepository;
import com.findear.main.board.query.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Transactional
@RequiredArgsConstructor
@Service
public class AcquiredBoardQueryService {

    private final AcquiredBoardQueryRepository acquiredBoardQueryRepository;
    private final CategoryRepository categoryRepository;
    private final int PAGE_SIZE = 10;

    public List<AcquiredBoardListResDto> findAll(Long memberId, Long categoryId, String sDate, String eDate, String keyword, int pageNo) {
        List<AcquiredBoard> acquiredBoards = acquiredBoardQueryRepository.findAll();
        Stream<AcquiredBoard> stream = acquiredBoards.stream();

        // filtering
        if (memberId != null) {
            stream = stream.filter(acquired -> acquired.getBoard().getMember().getId().equals(memberId));
        }
        if (categoryId != null) {
            String categoryName = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("invalid board category ID"))
                    .getCategoryName();

            stream = stream.filter(acquired -> acquired.getBoard().getCategoryName().equals(categoryName));
        }
        if (sDate != null || eDate != null) {
            stream = stream.filter(
                    acquired -> !acquired.getAcquiredAt().isBefore(sDate != null ? LocalDate.parse(sDate) : LocalDate.parse(eDate).minusMonths(6))
                            && !acquired.getAcquiredAt().isAfter(eDate != null ? LocalDate.parse(eDate) : LocalDate.now())
            );
        }
        if (keyword != null) {
            stream = stream.filter(acquired -> acquired.getBoard().getProductName().contains(keyword)
                    || acquired.getAddress().contains(keyword)
                    || acquired.getName().contains(keyword)
                    || (acquired.getBoard().getDescription() != null
                    ? acquired.getBoard().getDescription() : "").contains(keyword));
        }

        List<AcquiredBoardListResDto> filtered = stream
                .map(AcquiredBoardListResDto::of)
                .toList();

        // paging
        int eIdx = PAGE_SIZE * pageNo;
        int sIdx = eIdx - PAGE_SIZE;
        if (sIdx >= filtered.size()) return null;
        return filtered.subList(sIdx, Math.min(eIdx, filtered.size()));
    }

    public AcquiredBoardDetailResDto findById(Long boardId) {
        AcquiredBoard acquiredBoard = acquiredBoardQueryRepository.findByBoardId(boardId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게시물입니다."));

        return AcquiredBoardDetailResDto.of(acquiredBoard);
    }
}
