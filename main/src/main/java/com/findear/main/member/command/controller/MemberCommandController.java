package com.findear.main.member.command.controller;

import com.findear.main.common.response.SuccessResponse;
import com.findear.main.member.command.dto.BriefMemberDto;
import com.findear.main.member.command.dto.LoginReqDto;
import com.findear.main.member.command.dto.ModifyMemberReqDto;
import com.findear.main.member.command.dto.RegisterReqDto;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.command.service.MemberCommandService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/members")
@RestController
public class MemberCommandController {

    private final MemberCommandService memberCommandService;

    public MemberCommandController(MemberCommandService memberCommandService) {
        this.memberCommandService = memberCommandService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterReqDto registerReqDto) {
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "가입되었습니다.",
                memberCommandService.register(registerReqDto)));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<?> changeToManager(@PathVariable Long memberId,
                                             @RequestBody AgencyDto agencyDto) {
        memberCommandService.changeToManager(memberId, agencyDto);
        return ResponseEntity.ok(new SuccessResponse(HttpStatus.OK.value(), "변경되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto) {
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "로그인에 성공하였습니다.",
                memberCommandService.login(loginReqDto)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long memberId) {
        memberCommandService.logout(memberId);
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "로그아웃되었습니다."));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<?> modifyMember(@AuthenticationPrincipal Long memberId,
                                          @RequestBody ModifyMemberReqDto modifyMemberReqDto) {

        BriefMemberDto briefMemberDto = memberCommandService.modifyMember(memberId, modifyMemberReqDto);

        return ResponseEntity
                .ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), "변경되었습니다.",
                        briefMemberDto));
    }

    @PatchMapping("/{targetMemberId}/delete")
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal Long requestMemberId,
                                          @PathVariable Long targetMemberId) {
        memberCommandService.deleteMember(requestMemberId, targetMemberId);
        return ResponseEntity
                .ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), "탈퇴되었습니다."));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("refresh-token");
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                "발급되었습니다.",
                memberCommandService.refreshAccessToken(refreshToken)));
    }
}
