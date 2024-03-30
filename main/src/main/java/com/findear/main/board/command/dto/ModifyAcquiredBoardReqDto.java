package com.findear.main.board.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ModifyAcquiredBoardReqDto {
    private Long memberId;
    private Long boardId;
    private String color;
    private List<String> imgUrls;
    private String category;
    private String acquiredAt;
}
