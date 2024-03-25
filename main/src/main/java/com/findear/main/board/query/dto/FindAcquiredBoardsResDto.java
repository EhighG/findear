package com.findear.main.board.query.dto;

import com.findear.main.member.command.dto.LoginResAgencyDto;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindAcquiredBoardsResDto {
    private Long boardId;
    private String productName;
    private String category;
    private String thumbnailUrl;
    private LoginResAgencyDto agency;
    private LocalDateTime acquiredAt;

    public String getAcquiredAt() {
        return this.acquiredAt.format(DateTimeFormatter.ofPattern(("yyyy-MM-dd")));
    }
}
