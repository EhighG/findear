package com.findear.main.member.common.service;

import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.common.repository.MemberRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // redis로 대체될 저장소들
    private Map<String, MemberDto> accessTokens = new HashMap<>();
    private Map<String, MemberDto> refreshTokens = new HashMap<>();

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.memberRepository = memberRepository;
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
        MemberDto memberDto = MemberDto.of(
                memberRepository.findByEmail(loginReqDto.getEmail())
                        .orElseThrow(() -> new UsernameNotFoundException("유저 정보가 존재하지 않습니다."))
        );
        verifyPassword(loginReqDto.getPassword(), memberDto);
        return makeTokens(memberDto);
    }

    public MemberDto findById(Long memberId) {
        Member wrongMemberId = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("wrong memberId"));
        MemberDto memberDto = MemberDto.of(
                wrongMemberId
        );
        return memberDto;
    }

    public MemberDto verifyAccessToken(String accessToken) {
        MemberDto storedMember = accessTokens.get(accessToken);
        if (storedMember == null || !storedMember.getId().equals(jwtService.getMemberId(accessToken))) {
            throw new AuthenticationServiceException("401 unauthorized / accessToken mismatch");
        }
        return storedMember;
    }

    public MemberDto verifyRefreshToken(String refreshToken) {
        MemberDto storedMember = accessTokens.get(refreshToken);
        if (storedMember == null || !storedMember.getId().equals(jwtService.getMemberId(refreshToken))) {
            throw new AuthenticationServiceException("401 unauthorized / refreshToken mismatch");
        }
        return storedMember;
    }

    private LoginResDto makeTokens(MemberDto memberDto) {
        String accessToken = jwtService.createAccessToken(memberDto.getId());
        String refreshToken = jwtService.createRefreshToken(memberDto.getId());
        accessTokens.put(accessToken, memberDto);
        refreshTokens.put(refreshToken, memberDto);

        return LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void verifyPassword(String password, MemberDto member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }
}
