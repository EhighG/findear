package com.findear.main.board.command.service;

import com.findear.main.board.command.dto.PostLostBoardReqDto;
import com.findear.main.board.command.repository.BoardCommandRepository;
import com.findear.main.board.command.repository.ImgFileRepository;
import com.findear.main.board.command.repository.LostBoardCommandRepository;
import com.findear.main.board.common.domain.*;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.query.service.MemberQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardCommandService {

    private final LostBoardCommandRepository lostBoardCommandRepository;
    private final MemberQueryService memberQueryService;
    private final ImgFileRepository imgFileRepository;
    private final BoardCommandRepository boardCommandRepository;
    public Long register(PostLostBoardReqDto postLostBoardReqDto) {
        Member member = memberQueryService.internalFindById(postLostBoardReqDto.getMemberId());

        BoardDto boardDto = BoardDto.builder()
                .productName(postLostBoardReqDto.getProductName())
                .member(MemberDto.of(member))
                .color(postLostBoardReqDto.getColor())
                .description(postLostBoardReqDto.getContent())
                .thumbnailUrl(postLostBoardReqDto.getImgUrls().isEmpty() ?
                        null : postLostBoardReqDto.getImgUrls().get(0))
                .categoryName(postLostBoardReqDto.getCategory())
                .build();
        Board savedBoard = boardCommandRepository.save(boardDto.toEntity());
        // 이미지 등록
        List<ImgFile> imgFiles = new ArrayList<>();
        for (String imgUrl : postLostBoardReqDto.getImgUrls()) {
            ImgFile imgFile = new ImgFile(savedBoard, imgUrl);
            ImgFile savedFile = imgFileRepository.save(imgFile);
            imgFiles.add(savedFile);
        }
        savedBoard.updateImgFiles(imgFiles);

        LostBoardDto lostBoardDto = LostBoardDto.builder()
                .board(BoardDto.of(savedBoard))
                .lostAt(postLostBoardReqDto.getLostAt())
                .suspiciousPlace(postLostBoardReqDto.getSuspiciousPlace())
                .xPos(Float.parseFloat(postLostBoardReqDto.getXpos()))
                .yPos(Float.parseFloat(postLostBoardReqDto.getYpos()))
                .build();

        LostBoard saveResult = lostBoardCommandRepository.save(lostBoardDto.toEntity());
        return saveResult.getBoard().getId();
    }
}
