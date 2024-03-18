package com.findear.main.member.common.controller;

import com.findear.main.member.common.dto.LoginReqDto;
import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.common.dto.RegisterReqDto;
import com.findear.main.member.common.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReqDto registerReqDto) {
        return ResponseEntity.ok(memberService.register(registerReqDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto) {
        return ResponseEntity.ok(memberService.login(loginReqDto));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<?> findById(@PathVariable Long memberId) {
        MemberDto byId = memberService.findById(memberId);
        return ResponseEntity.ok(byId);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("refresh-token");
        return ResponseEntity
                .ok(memberService.refreshAccessToken(refreshToken));
    }
}
