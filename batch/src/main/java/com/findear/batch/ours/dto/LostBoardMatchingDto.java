package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class LostBoardMatchingDto {

    private Long boardId;

    private String productName;

    private String color;

    private String categoryName;

    private String description;

    private String lostAt;

    private Float xPos;

    private Float yPos;

}
