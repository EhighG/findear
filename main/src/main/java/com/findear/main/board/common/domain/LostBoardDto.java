package com.findear.main.board.common.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class LostBoardDto {
    private Long id;

    private BoardDto board;

    private String lostAt;

    private String suspiciousPlace;

    private Float xPos;

    private Float yPos;

    @Builder
    public LostBoardDto(Long id, BoardDto board, LocalDate lostAt, String suspiciousPlace, Float xPos, Float yPos) {
        this.id = id;
        this.board = board;
        this.lostAt = lostAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.suspiciousPlace = suspiciousPlace;
        this.xPos = xPos;
        this.yPos = yPos;
    }
//
//    public static LostBoardDto of(LostBoard lostBoard) {
//        return LostBoardDto.builder()
//                .id(lostBoard.getId())
//                .board
//    }
}
