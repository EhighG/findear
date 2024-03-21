package com.findear.main.member.common.controller;

import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.common.dto.*;
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
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "가입되었습니다.",
                memberService.register(registerReqDto)));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<?> changeToManager(@PathVariable Long memberId,
                                             @RequestBody AgencyDto agencyDto) {
        memberService.changeToManager(memberId, agencyDto);
        return ResponseEntity.ok(new SuccessResponse(HttpStatus.OK.value(), "변경되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto) {
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "로그인에 성공하였습니다.",
                memberService.login(loginReqDto)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long memberId) {
        memberService.logout(memberId);
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "로그아웃되었습니다."));
    }

    @PostMapping("/duplicate")
    public ResponseEntity<?> checkDuplicate(@RequestBody String phoneNumber) {
        return ResponseEntity
                .ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), "요청에 성공하였습니다.",
                        memberService.checkDuplicate(phoneNumber)));
    }

    @GetMapping("/{targetMemberId}")
    public ResponseEntity<?> findById(@PathVariable Long targetMemberId,
                                      @AuthenticationPrincipal Long requestMemberId) {
        FindMemberResDto foundMember = memberService.findById(targetMemberId, requestMemberId);
        return ResponseEntity
                .ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), "요청에 성공하였습니다.",
                        foundMember));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<?> modifyMember(@AuthenticationPrincipal Long memberId,
                                          @RequestBody ModifyMemberReqDto modifyMemberReqDto) {

        BriefMemberDto briefMemberDto = memberService.modifyMember(memberId, modifyMemberReqDto);

        return ResponseEntity
                .ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), "변경되었습니다.",
                        briefMemberDto));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("refresh-token");
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "발급되었습니다.",
                memberService.refreshAccessToken(refreshToken)));
    }
}
