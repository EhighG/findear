package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class LostBoardMatchingDto {

    private Long boardId;

    private String productName;

    private String color;

    private String categoryName;

    private String description;

    private LocalDate lostAt;

    private Float xPos;

    private Float yPos;

}
