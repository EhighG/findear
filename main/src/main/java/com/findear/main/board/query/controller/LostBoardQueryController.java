package com.findear.main.board.query.controller;

import com.findear.main.board.common.domain.BoardDto;
import com.findear.main.board.common.domain.LostBoardDto;
import com.findear.main.board.common.domain.imgFile;
import com.findear.main.board.query.dto.FindLostBoardsResDto;
import com.findear.main.board.query.service.LostBoardQueryService;
import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.command.dto.BriefMemberDto;
import com.findear.main.member.common.domain.Role;
import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.query.dto.FindMemberListResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/losts")
@RestController
public class LostBoardQueryController {

    private final LostBoardQueryService lostBoardQueryService;

    @GetMapping
    public ResponseEntity<?> findLosts(@RequestParam(required = false) Long memberId,
                                       @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
//        return ResponseEntity
//                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성곻했습니다.",
//                        lostBoardQueryService.findLostBoards(memberId, pageNo)));
        // dummy
        List<FindLostBoardsResDto> dummy = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            dummy.add(FindLostBoardsResDto.builder()
                    .boardId((long) i)
                    .productName("ProductName " + i)
                    .category("CategoryName " + i)
                    .thumbnailUrl("sampleImgUrl " + i)
                    .lostAt("2024-03-" + String.format("%02d", i))
                    .writer(new BriefMemberDto(1L, "010-9999-9999"))
                    .build());
        }
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.",
                        dummy));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> findById(@PathVariable Long boardId) {
        LostBoardDto dummy = LostBoardDto.builder()
                .id(1L)
                .board(BoardDto.builder()
                        .id(1L)
                        .productName("productName 1")
                        .member(MemberDto.builder()
                                .id(1L)
                                .phoneNumber("010-9999-9999")
                                .role(Role.NORMAL)
                                .build()
                        )
                        .imgFileList(Arrays.asList(new imgFile(1L, "smapleUrl 1"), new imgFile(2L, "smapleUrl 2")))
                        .color("빨강")
                        .description("description 1")
                        .registeredAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .build())
                .lostAt(LocalDate.now())
                .suspiciousPlace("역삼역 3번출구")
                .xPos(52.12314f)
                .yPos(52.12312f)
                .build();
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "요청에 성공했습니다.", dummy));
    }
}
