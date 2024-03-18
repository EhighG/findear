package com.findear.main.member.common.controller;

import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.common.dto.LoginReqDto;
import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.common.dto.RegisterReqDto;
import com.findear.main.member.common.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterReqDto registerReqDto) {
        try {
            return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                    "가입되었습니다.",
                    memberService.register(registerReqDto)));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto) {
        try {
            return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                    "로그인에 성공하였습니다.",
                    memberService.login(loginReqDto)));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<?> findById(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                    "발급되었습니다.",
                    memberService.findById(memberId)));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        try {
            String refreshToken = request.getHeader("refresh-token");
            return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                    "발급되었습니다.",
                    memberService.refreshAccessToken(refreshToken)));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}
