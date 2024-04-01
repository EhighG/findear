package com.findear.main.board.command.service;

import com.findear.main.board.command.dto.*;
import com.findear.main.board.command.repository.BoardCommandRepository;
import com.findear.main.board.command.repository.ImgFileRepository;
import com.findear.main.board.command.repository.LostBoardCommandRepository;
import com.findear.main.board.common.domain.*;
import com.findear.main.board.query.repository.BoardQueryRepository;
import com.findear.main.board.query.repository.LostBoardQueryRepository;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.query.service.MemberQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LostBoardCommandService {

    private final LostBoardCommandRepository lostBoardCommandRepository;
    private final MemberQueryService memberQueryService;
    private final ImgFileRepository imgFileRepository;
    private final BoardCommandRepository boardCommandRepository;
    private final BoardQueryRepository boardQueryRepository;
    private final LostBoardQueryRepository lostBoardQueryRepository;

    public PostLostBoardResDto register(PostLostBoardReqDto postLostBoardReqDto) {
        Member member = memberQueryService.internalFindById(postLostBoardReqDto.getMemberId());

        BoardDto boardDto = BoardDto.builder()
                .productName(postLostBoardReqDto.getProductName())
                .member(MemberDto.of(member))
                .color(postLostBoardReqDto.getColor())
                .description(postLostBoardReqDto.getContent())
                .thumbnailUrl(postLostBoardReqDto.getImgUrls().isEmpty() ?
                        null : postLostBoardReqDto.getImgUrls().get(0))
                .categoryName(postLostBoardReqDto.getCategory())
                .isLost(true)
                .status(BoardStatus.ONGOING)
                .deleteYn(false)
                .build();
        Board savedBoard = boardCommandRepository.save(boardDto.toEntity());

        log.info("이미지 등록");
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

        MatchingFindearDatasReqDto matchingFindearDatasReqDto = MatchingFindearDatasReqDto.builder()
                .lostBoardId(saveResult.getId())
                .productName(saveResult.getBoard().getProductName())
                .color(saveResult.getBoard().getColor())
                .categoryName(saveResult.getBoard().getCategoryName())
                .description(saveResult.getBoard().getAiDescription())
                .lostAt(saveResult.getLostAt().toString())
                .xPos(saveResult.getXPos())
                .yPos(saveResult.getYPos())
                .build();

        log.info("batch 서버로 요청 로직");
        // batch 서버로 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        HttpEntity<?> requestEntity = new HttpEntity<>(matchingFindearDatasReqDto, headers);

        String serverURL = "https://j10a706.p.ssafy.io/batch/findear/matching";
//        String serverURL = "http://localhost:8082/findear/matching";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

        System.out.println("response : " + response.getBody());

        List<Map<String, Object>> resultList = (List<Map<String, Object>> ) response.getBody().get("result");

        log.info("매칭 결과 : " + resultList);

        List<MatchingFindearDatasToAiResDto> result = new ArrayList<>();

        for(Map<String, Object> res : resultList) {

            MatchingFindearDatasToAiResDto matchingFindearDatasToAiResDto = MatchingFindearDatasToAiResDto.builder()
                    .lostBoardId(res.get("lostBoardId"))
                    .acquiredBoardId(res.get("acquiredBoardId"))
                    .similarityRate(res.get("similarityRate")).build();

            result.add(matchingFindearDatasToAiResDto);
        }

        return new PostLostBoardResDto(savedBoard.getId(), result);
    }

    public Long modify(ModifyLostBoardReqDto modifyReqDto) {
        LostBoard lostBoard = lostBoardQueryRepository.findByBoardId(modifyReqDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        if (modifyReqDto.getImgUrls() != null && !modifyReqDto.getImgUrls().isEmpty()) {
            List<ImgFile> imgFileList = modifyReqDto.getImgUrls().stream()
//                .map(imgUrl -> imgFileRepository.findByImgUrl(imgUrl)
                    .map(imgUrl -> imgFileRepository.findFirstByImgUrl(imgUrl) // 개발환경용
                            .orElse(imgFileRepository.save(new ImgFile(lostBoard.getBoard(), imgUrl)))
                    ).toList();
            modifyReqDto.setImgFileList(imgFileList);
        }
        lostBoard.modify(modifyReqDto);

        return lostBoard.getBoard().getId();
    }

    public void remove(Long boardId, Long memberId) {
        Board board = boardQueryRepository.findByIdAndDeleteYnFalse(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다."));
        if (!board.getMember().getId().equals(memberId)) {
            throw new AuthorizationServiceException("권한이 없습니다.");
        }
        board.remove();
    }

}
