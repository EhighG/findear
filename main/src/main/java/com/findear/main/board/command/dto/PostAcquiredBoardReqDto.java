package com.findear.main.board.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostAcquiredBoardReqDto {

    @Setter
    private String productName;
    @Setter
    private List<String> imgUrls;
    @Setter
    private Long memberId;
//    private String description;
//    private String categoryName;
//    private String color;
//    private String address;
//    private String agencyName;
//    private Float xPos;
//    private Float yPos;
}
