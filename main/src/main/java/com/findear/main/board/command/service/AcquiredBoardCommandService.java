package com.findear.main.board.command.service;

import com.findear.main.board.command.dto.ModelServerResponseDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AcquiredBoardCommandService {

    private final AcquiredBoardCommandRepository acquiredBoardCommandRepository;
    private final BoardCommandRepository boardCommandRepository;
    private final MemberQueryService memberQueryService;
    private final ImgFileRepository imgFileRepository;

    public static String MODEL_SERVER_URL = "https://j10a706.p.ssafy.io/";

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

        // 비동기 처리됨
        sendAutoFillRequest(savedAcquiredBoard)
                .subscribe(
                        response -> fillColumns(savedAcquiredBoard, response),
                        error -> log.info("습득물 컬럼 자동 업데이트 실패. \nerror = " + error)
                );

        return savedBoard.getId();
    }

    private Mono<ModelServerResponseDto> sendAutoFillRequest(AcquiredBoard notFilledBoard) {
        WebClient client = WebClient.builder()
                .baseUrl(MODEL_SERVER_URL)
                .build();

        NotFilledBoardDto notFilledBoardDto = NotFilledBoardDto.of(notFilledBoard);

        return client
                .post()
                .uri("/process")
                .bodyValue(notFilledBoardDto)
                .retrieve()
                .bodyToMono(ModelServerResponseDto.class);
    }

    private void fillColumns(AcquiredBoard notFilledBoard, ModelServerResponseDto modelServerResponseDto) {
        log.info("modelServerResponse = " + modelServerResponseDto);
        notFilledBoard.updateAutoFilledColumn(modelServerResponseDto.getResult());
        boardCommandRepository.save(notFilledBoard.getBoard());
    }
}
