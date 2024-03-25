package com.findear.main.board.query.controller;

import com.findear.main.board.common.domain.AcquiredBoardDto;
import com.findear.main.board.query.dto.FindAcquiredBoardsResDto;
import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.command.dto.BriefMemberDto;
import com.findear.main.member.command.dto.LoginResAgencyDto;
import com.findear.main.member.common.domain.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/acquisitions")
@RestController
public class AcquiredBoardQueryController {

    @GetMapping
    public ResponseEntity<?> findAcquiredBoards(@RequestParam(required=false) Long categoryId,
                                                @RequestParam(required = false) Long memberId,
                                                @RequestParam(required = false) String sDate,
                                                @RequestParam(required = false) String eDate,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        List<FindAcquiredBoardsResDto> dummy = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            FindAcquiredBoardsResDto findAcquiredBoardsResDto = FindAcquiredBoardsResDto.builder()
                    .boardId((long) i)
                    .acquiredAt(LocalDateTime.now())
                    .productName("productName " + i)
                    .category("category " + i)
                    .thumbnailUrl("thumbnail url " + i)
                    .agency(LoginResAgencyDto.builder()
                            .id((long) i)
                            .name("agencyName " + i)
                            .address("address " + i)
                            .build())
                    .build();
            dummy.add(findAcquiredBoardsResDto);
        }
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.", dummy));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> findById(@PathVariable Long boardId) {
        AcquiredBoardDto acquiredBoardDto = AcquiredBoardDto.builder()
                .acquisitionId(1L)
                .boardId(2L)
//                .board(BoardDto.builder()
//                        .id(1L)
//                        .productName("productName 1")
//                        .member(MemberDto.builder()
//                                .id(1L)
//                                .phoneNumber("010-9999-9999")
//                                .role(Role.NORMAL)
//                                .build()
//                        )
//                        .imgFileList(Arrays.asList(new imgFile(1L, "smapleUrl 1"), new imgFile(2L, "smapleUrl 2")))
//                        .color("빨강")
//                        .description("description 1")
//                        .registeredAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
//                        .build())
                .member(
                        new BriefMemberDto(1L, "010-9999-9999", Role.NORMAL)
                )
                .productName("productName 1")
                .imgUrlList(Arrays.asList("smapleUrl 1", "smapleUrl 2"))
                .color("빨강")
                .description("description 1")
                .registeredAt(LocalDate.now())
                .categoryName("category 1")
                .address("address " + 1)
                .name("name " + 1)
                .acquiredAt(LocalDate.now())
                .xPos(12.13f)
                .yPos(12.13f)
                .build();
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.", acquiredBoardDto));
    }
}
