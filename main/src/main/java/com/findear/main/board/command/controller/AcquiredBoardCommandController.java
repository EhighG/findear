package com.findear.main.board.command.controller;

import com.findear.main.board.command.dto.ModifyAcquiredBoardReqDto;
import com.findear.main.board.command.dto.PostAcquiredBoardReqDto;
import com.findear.main.board.command.service.AcquiredBoardCommandService;
import com.findear.main.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/acquisitions")
@RequiredArgsConstructor
@RestController
public class AcquiredBoardCommandController {

    private final AcquiredBoardCommandService acquiredBoardCommandService;

    @PostMapping
    public ResponseEntity<?> register(@AuthenticationPrincipal Long memberId,
                                      @RequestBody PostAcquiredBoardReqDto postAcquiredBoardReqDto) {
        postAcquiredBoardReqDto.setMemberId(memberId);
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "등록되었습니다.",
                        acquiredBoardCommandService.register(postAcquiredBoardReqDto)));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<?> modify(@AuthenticationPrincipal Long memberId,
                                    @PathVariable Long boardId,
                                    @RequestBody ModifyAcquiredBoardReqDto modifyReqDto) {
        modifyReqDto.setMemberId(memberId);
        modifyReqDto.setBoardId(boardId);
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(),
                        "변경되었습니다.",
                        acquiredBoardCommandService.modify(modifyReqDto)));
    }
}
