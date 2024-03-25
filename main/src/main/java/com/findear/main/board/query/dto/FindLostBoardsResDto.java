package com.findear.main.board.query.dto;

import com.findear.main.board.common.domain.Board;
import com.findear.main.board.common.domain.Category;
import com.findear.main.board.common.domain.LostBoard;
import com.findear.main.member.command.dto.BriefMemberDto;
import com.findear.main.member.common.domain.Member;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor

public class FindLostBoardsResDto {
    private Long boardId;
    private String productName;
    private String category;
    private String thumbnailUrl;
    private String lostAt;
    private BriefMemberDto writer;

    @Builder
    public FindLostBoardsResDto(Long boardId, String productName, String category, String thumbnailUrl, String lostAt, BriefMemberDto writer) {
        this.boardId = boardId;
        this.productName = productName;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.lostAt = lostAt;
        this.writer = writer;
    }

    public FindLostBoardsResDto of(LostBoard lostBoard) {
        Board board = lostBoard.getBoard();
        Member writer = board.getMember();

        return FindLostBoardsResDto.builder()
                .boardId(board.getId())
                .productName(board.getProductName())
                .category(board.getCategoryName())
                .thumbnailUrl(board.getThumbnailUrl())
                .lostAt(lostBoard.getLostAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .writer(new BriefMemberDto(writer.getId(), writer.getPhoneNumber()))
                .build();
    }
}
