package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class AcquiredBoardMatchingDto {

    private String acquiredBoardId;

    private String productName;

    private String color;

    private String categoryName;

    private String description;

    private String xPos;

    private String yPos;

    private String registeredAt;
//    private Long acquiredBoardId;
//
//    private String productName;
//
//    private String color;
//
//    private String categoryName;
//
//    private String description;
//
//    private Float xPos;
//
//    private Float yPos;
//
//    private LocalDateTime registeredAt;
}
