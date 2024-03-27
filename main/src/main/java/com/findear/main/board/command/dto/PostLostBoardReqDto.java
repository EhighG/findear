package com.findear.main.board.command.dto;

import com.findear.main.board.common.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class PostLostBoardReqDto {
    private String productName;
    private Long memberId;
    private String category;
    private String color;
    private String content;
    private List<String> imgUrls;
    private String lostAt;
    private String xpos;
    private String ypos;
    private String suspiciousPlace;
}
