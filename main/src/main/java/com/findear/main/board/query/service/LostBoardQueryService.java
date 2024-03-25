package com.findear.main.board.query.service;


import com.findear.main.board.common.domain.LostBoard;
import com.findear.main.board.query.dto.LostBoardListResDto;
import com.findear.main.board.query.repository.LostBoardQueryRepository;
import com.findear.main.member.query.dto.FindMemberListResDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardQueryService {

    private final LostBoardQueryRepository lostBoardQueryRepository;
    private final int PAGE_SIZE = 3;

    public List<LostBoardListResDto> findAllByMemberId(Long memberId, int pageNo) {
        List<LostBoard> lostBoards = lostBoardQueryRepository.findAll();

        if (memberId != null) {
            return lostBoards.stream()
                    .filter(lost -> lost.getBoard().getMember().getId().equals(memberId))
                    .map(LostBoardListResDto::of)
                    .collect(Collectors.toList());
        }
        List<LostBoardListResDto> filtered = lostBoards.stream()
                .map(LostBoardListResDto::of)
                .toList();

        int eIdx = PAGE_SIZE * pageNo;
        int sIdx = eIdx - PAGE_SIZE;
        if (sIdx >= filtered.size()) return null;
        return filtered.subList(sIdx, Math.min(eIdx, filtered.size()));
    }
}
