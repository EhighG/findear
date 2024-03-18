package com.findear.main.security;

import com.findear.main.member.common.dto.MemberDto;
import com.findear.main.member.common.service.JwtService;
import com.findear.main.member.common.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtAuthenticationProvider {

    private final MemberService memberService;

    public JwtAuthenticationProvider(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * param : unauthenticated 이고, accessToken만 담고 있는 객체
     * return : authenticated이고, member가 추가된 객체
     */
    public Authentication authenticateAccessToken(String accessToken) {
        MemberDto memberDto = memberService.verifyAccessToken(accessToken);

        return JwtAuthenticationToken.authenticated(memberDto, accessToken,
                Arrays.asList(new SimpleGrantedAuthority(memberDto.getRole().getValue())));
    }

    /**
     * param : unauthenticated 이고, refreshToken만 담고 있는 객체
     * return : authenticated이고, member가 추가된 객체
     */
    public Authentication authenticationRefreshToken(String refreshToken) {
        MemberDto memberDto = memberService.verifyRefreshToken(refreshToken);

        return JwtAuthenticationToken.authenticated(memberDto, refreshToken,
                Arrays.asList(new SimpleGrantedAuthority(memberDto.getRole().getValue())));
    }
}
