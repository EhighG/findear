package com.findear.batch.ours.controller;

import com.findear.batch.common.response.SuccessResponse;
import com.findear.batch.ours.dto.LostBoardMatchingDto;
import com.findear.batch.ours.dto.MatchingFindearDatasToAiResDto;
import com.findear.batch.ours.service.FindearDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/findear")
@RestController
public class FindearDataController {

    private final FindearDataService findearDataService;

    @PostMapping("/matching")
    public ResponseEntity<?> matchingFindearDatas(@RequestBody LostBoardMatchingDto lostBoardMatchingDto) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        log.info("분실물 매칭");
        List<MatchingFindearDatasToAiResDto> result = findearDataService.matchingFindearDatas(lostBoardMatchingDto);

        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(), "모든 데이터 조회 성공", result));

    }

    @PostMapping("/matching/batch")
    public ResponseEntity<?> matchingFindearDatasBatch() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        List<MatchingFindearDatasToAiResDto> result = findearDataService.matchingFindearDatasBatch();

        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(), "모든 데이터 조회 성공", result));

    }
}