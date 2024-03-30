package com.findear.main.board.command.service;

import com.findear.main.board.command.dto.MatchingFindearDatasReqDto;
import com.findear.main.board.command.dto.MatchingFindearDatasToAiResDto;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public List<MatchingFindearDatasToAiResDto> register(PostLostBoardReqDto postLostBoardReqDto) {
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
                .lostBoardId(saveResult.getBoard().getId())
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

        return result;
    }

}
