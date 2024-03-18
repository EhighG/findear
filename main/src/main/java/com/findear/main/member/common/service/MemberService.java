package com.findear.main.member.common.service;

import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.common.repository.MemberRepository;
import com.findear.main.member.common.repository.RefreshTokenRepository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // redis로 대체될 저장소들
//    private Map<String, MemberDto> accessTokens = new HashMap<>();
//    private Map<Long, String> refreshTokens = new HashMap<>(); // Map<AccessToken, RefreshToken>

    public MemberService(MemberRepository memberRepository, RefreshTokenRepository tokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(RegisterReqDto registerReqDto) {
        Member registerMember = Member.builder()
                .email(registerReqDto.getEmail())
                .nickname(registerReqDto.getNickname())
                .password(passwordEncoder.encode(registerReqDto.getPassword()))
                .phoneNumber(registerReqDto.getPhoneNumber())
                .role(registerReqDto.getRole())
                .build();
        Member savedMember = memberRepository.save(registerMember);

        return savedMember.getEmail();
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        Member member = memberRepository.findByEmail(loginReqDto.getEmail())
                        .orElseThrow(() -> new UsernameNotFoundException("유저 정보가 존재하지 않습니다."));
        MemberDto memberDto = MemberDto.of(member);

        verifyPassword(loginReqDto.getPassword(), memberDto);

        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setMember(memberDto);
        makeTokens(memberDto, loginResDto);
        return loginResDto;
    }

    public MemberDto findById(Long memberId) {
        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("wrong memberId"));
        MemberDto memberDto = MemberDto.of(
                foundMember
        );
        return memberDto;
    }

    public MemberDto verifyAccessToken(String accessToken) {
        if (jwtService.isExpired(accessToken)) {
            throw new AuthenticationServiceException("401 unauthorized");
        }
        Long memberId = jwtService.getMemberId(accessToken);
        String refreshToken = tokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));

        if (refreshToken == null || !jwtService.getMemberId(refreshToken).equals(memberId)) {
            throw new AuthenticationServiceException("401 unauthorized");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원정보가 존재하지 않습니다."));

        return MemberDto.of(member);
    }

    public MemberDto verifyRefreshToken(String refreshToken) {
        Long memberId = jwtService.getMemberId(refreshToken);
//        String storedToken = refreshTokens.get(memberId);
        String storedToken = tokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));

        if (!refreshToken.equals(storedToken)) {
            throw new RuntimeException("401 unauthorized");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("invalid memberId"));
        return MemberDto.of(member);
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        // refreshToken 검증은 마친 상태
        Long memberId = jwtService.getMemberId(refreshToken);
        String accessToken = jwtService.createAccessToken(memberId);
        Map<String, String> result = new HashMap<>();
        result.put("tokenType", JwtService.TOKEN_TYPE);
        result.put("accessToken", accessToken);
        return result;
    }

    private LoginResDto makeTokens(MemberDto memberDto, LoginResDto loginResDto) {
        String accessToken = jwtService.createAccessToken(memberDto.getId());
        String refreshToken = jwtService.createRefreshToken(memberDto.getId());
        tokenRepository.save(memberDto.getId(), refreshToken);

        loginResDto.setAccessToken(accessToken);
        loginResDto.setRefreshToken(refreshToken);
        return loginResDto;
    }

    private void verifyPassword(String password, MemberDto member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }
}
