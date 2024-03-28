package com.findear.main.board.command.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MatchingFindearDatasReqDto {

    private Long boardId;

    private String productName;

    private String color;

    private String categoryName;

    private String aiDescription;

    private String lostAt;

    private Float xPos;

    private Float yPos;
}
