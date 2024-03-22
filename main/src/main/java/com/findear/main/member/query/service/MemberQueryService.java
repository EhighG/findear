package com.findear.main.member.query.service;

import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.query.dto.FindMemberResDto;
import com.findear.main.member.query.repository.MemberQueryRepository;
import com.findear.main.security.RefreshTokenRepository;
import com.findear.main.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberQueryService {

    private final MemberQueryRepository memberQueryRepository;
    private final RefreshTokenRepository tokenRepository;
    private final JwtService jwtService;

    public Long getAuthenticatedMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인되지 않은 상태입니다.");
        }
        return (Long) authentication.getPrincipal();
    }

    public MemberDto findByPhoneNumber(String phoneNumber) {
        Member member = memberQueryRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("회원정보가 존재하지 않습니다."));
        return MemberDto.of(member);
    }

    public boolean checkDuplicate(String phoneNumber) {
        if (phoneNumber.charAt(0) == '"') {
            phoneNumber = phoneNumber.substring(1, phoneNumber.length() - 1);
        }
        return memberQueryRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    public Member internalFindById(Long memberId) {

        Member member = memberQueryRepository.findByIdWithAgency(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원정보가 존재하지 않습니다."));


        validMemberNotDeleted(member);
        return member;
    }

    public FindMemberResDto findMemberById(Long targetMemberId, Long requestMemberId) {
        Member member = internalFindById(targetMemberId);

        FindMemberResDto foundMember = FindMemberResDto.of(member);

        if (!foundMember.getMemberId().equals(requestMemberId)) {
            foundMember.setPhoneNumber(blindPhoneNumber(foundMember.getPhoneNumber()));
        }
        return foundMember;
    }

//    public List<Member> findMembers(String keyword) {
//        List<Member> members = memberQueryRepository.findAll();
//        return members.stream().filter((member) -> member.getPhoneNumber().contains(keyword)).collect(Collectors.toList());
//    }

    public MemberDto verifyAccessToken(String accessToken) {
        if (jwtService.isExpired(accessToken)) {
            throw new AuthenticationServiceException("401 unauthorized");
        }
        Long memberId = jwtService.getMemberId(accessToken);
        String refreshToken = tokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));

        if (!jwtService.getMemberId(refreshToken).equals(memberId)) {
            throw new AuthenticationServiceException("401 unauthorized");
        }

        Member member = memberQueryRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원정보가 존재하지 않습니다."));

        return MemberDto.of(member);
    }

    public MemberDto verifyRefreshToken(String refreshToken) {
        Long memberId = jwtService.getMemberId(refreshToken);
        String storedToken = tokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));

        if (!refreshToken.equals(storedToken)) {
            throw new AuthenticationServiceException("401 unauthorized");
        }
        Member member = memberQueryRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));
        return MemberDto.of(member);
    }

    private void validMemberNotDeleted(Member member) {
        if (member.getWithdrawalYn() != null && !member.getWithdrawalYn()) {
            throw new UsernameNotFoundException("회원정보가 존재하지 않습니다.");
        }
    }

    private String blindPhoneNumber(String phoneNumber) {
        int len = phoneNumber.length();
        if (len <= 4) return phoneNumber;
        return "****" + phoneNumber.substring(len - 4);
    }
}
