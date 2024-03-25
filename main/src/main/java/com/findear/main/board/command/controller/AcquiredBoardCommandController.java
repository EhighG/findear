package com.findear.main.board.command.controller;

import com.findear.main.board.command.dto.PostAcquiredBoardReqDto;
import com.findear.main.board.command.service.AcquiredBoardCommandService;
import com.findear.main.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/acquisitions")
@RequiredArgsConstructor
@RestController
public class AcquiredBoardCommandController {

    private final AcquiredBoardCommandService acquiredBoardCommandService;

    @PostMapping
    public ResponseEntity<?> register(@AuthenticationPrincipal Long memberId,
                                      @RequestBody PostAcquiredBoardReqDto postAcquiredBoardReqDto) {
        postAcquiredBoardReqDto.setMemberId(memberId);
        acquiredBoardCommandService.register(postAcquiredBoardReqDto);
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "등록되었습니다."));
    }
}
