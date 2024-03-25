package com.findear.main.board.query.service;


import com.findear.main.board.query.repository.LostBoardQueryRepository;
import com.findear.main.member.query.dto.FindMemberListResDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardQueryService {

    private final LostBoardQueryRepository lostBoardQueryRepository;

    public List<FindMemberListResDto> findLostBoards(Long memberId, int pageNo) {
        List<FindMemberListResDto> result = new ArrayList<>();

        return result;
    }
}
