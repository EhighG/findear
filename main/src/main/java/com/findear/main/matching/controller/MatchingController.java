package com.findear.main.matching.controller;

import com.findear.main.common.response.SuccessResponse;
import com.findear.main.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/matchings")
@RestController
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/brief")
    public ResponseEntity<?> getBestMatchings(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity
                .ok(new SuccessResponse(HttpStatus.OK.value(), "조회에 성공했습니다.",
                        matchingService.getBestMatchings(memberId)));
    }
}
