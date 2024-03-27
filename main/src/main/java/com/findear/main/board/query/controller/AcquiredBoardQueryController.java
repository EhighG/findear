package com.findear.main.board.query.controller;

import com.findear.main.board.common.domain.AcquiredBoardDto;
import com.findear.main.board.query.dto.AcquiredBoardListResDto;
import com.findear.main.board.query.service.AcquiredBoardQueryService;
import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.command.dto.BriefMemberDto;
import com.findear.main.member.command.dto.LoginResAgencyDto;
import com.findear.main.member.common.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/acquisitions")
@RestController
public class AcquiredBoardQueryController {

    private final AcquiredBoardQueryService acquiredBoardQueryService;

    @GetMapping
    public ResponseEntity<?> findAll(@RequestParam(required=false) Long categoryId,
                                                @RequestParam(required = false) Long memberId,
                                                @RequestParam(required = false) String sDate,
                                                @RequestParam(required = false) String eDate,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.",
                        acquiredBoardQueryService.findAll(memberId, categoryId, sDate, eDate, keyword, pageNo)));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> findById(@PathVariable Long boardId) {
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.", acquiredBoardQueryService.findById(boardId)));
    }

    @GetMapping("/lost112")
    public ResponseEntity<?> findAllInLost112(@RequestParam(required=false) Long categoryId,
                                              @RequestParam(required = false) String sDate,
                                              @RequestParam(required = false) String eDate,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.",
                        acquiredBoardQueryService.findAllInLost112(categoryId, sDate, eDate, keyword, pageNo)));
    }
}
