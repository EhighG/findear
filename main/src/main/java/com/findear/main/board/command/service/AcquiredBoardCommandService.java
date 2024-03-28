package com.findear.main.board.command.service;

import com.findear.main.board.command.dto.NotFilledBoardDto;
import com.findear.main.board.command.dto.PostAcquiredBoardReqDto;
import com.findear.main.board.command.repository.AcquiredBoardCommandRepository;
import com.findear.main.board.command.repository.BoardCommandRepository;
import com.findear.main.board.command.repository.ImgFileRepository;
import com.findear.main.board.common.domain.*;
import com.findear.main.member.common.domain.Agency;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.query.service.MemberQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class AcquiredBoardCommandService {

    private final AcquiredBoardCommandRepository acquiredBoardCommandRepository;
    private final BoardCommandRepository boardCommandRepository;
    private final MemberQueryService memberQueryService;
    private final ImgFileRepository imgFileRepository;

    public static String MODEL_SERVER_URL = "";

    public Long register(PostAcquiredBoardReqDto postAcquiredBoardReqDto) {
        Member manager = memberQueryService.internalFindById(postAcquiredBoardReqDto.getMemberId());
        Board savedBoard = boardCommandRepository.save(Board.builder()
                .productName(postAcquiredBoardReqDto.getProductName())
                .member(manager)
                .thumbnailUrl(postAcquiredBoardReqDto.getImgUrls().get(0))
                .deleteYn(false)
                .isLost(false)
                .build());

        List<ImgFile> imgFiles = new ArrayList<>();
        for (String imgUrl : postAcquiredBoardReqDto.getImgUrls()) {
            ImgFile imgFile = new ImgFile(savedBoard, imgUrl);
            ImgFile savedFile = imgFileRepository.save(imgFile);
            imgFiles.add(savedFile);
        }
        savedBoard.updateImgFiles(imgFiles);
        Agency agency = manager.getAgency();
        AcquiredBoard acquiredBoard = AcquiredBoard.builder()
                .board(savedBoard)
                .address(agency.getAddress())
                .name(agency.getName())
                .xPos(agency.getXPos())
                .yPos(agency.getYPos())
                .build();
        AcquiredBoard savedAcquiredBoard = acquiredBoardCommandRepository.save(acquiredBoard);

//        sendAutoFillRequest(savedAcquiredBoard);

        return savedBoard.getId();
    }

//    private AcquiredBoard sendAutoFillRequest(AcquiredBoard notFilledBoard) {
//        WebClient client = WebClient.builder()
//                .baseUrl(MODEL_SERVER_URL)
//                .build();
//
//        NotFilledBoardDto notFilledBoardDto = NotFilledBoardDto.of(notFilledBoard);
//
//        AcquiredBoard filledBoard = client
//                .post()
//                .uri("/autofill")
//                .bodyValue(notFilledBoardDto)
//                .retrieve()
//                .bodyToMono(AcquiredBoard.class)
//                .block();
//
//        // updateAutoFilledColumn() 작성 중
//        notFilledBoard.updateAutoFilledColumn(filledBoard.getBoard());
//    }
}
