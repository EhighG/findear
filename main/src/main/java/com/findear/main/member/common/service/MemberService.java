package com.findear.main.member.common.service;

import com.findear.main.member.common.domain.Agency;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.domain.Role;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.common.repository.AgencyRepository;
import com.findear.main.member.common.repository.MemberRepository;
import com.findear.main.member.common.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AgencyRepository agencyRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public MemberService(MemberRepository memberRepository, AgencyRepository agencyRepository, RefreshTokenRepository tokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.agencyRepository = agencyRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * @param registerReqDto - phoneNumber, password
     * @return phoneNumber
     * @throws IllegalArgumentException - phoneNumber 중복
     */
    public String register(RegisterReqDto registerReqDto) {
        if (memberRepository.findByPhoneNumber(registerReqDto.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("회원정보가 이미 있습니다.");
        }
        Member registerMember = Member.builder()
                .phoneNumber(registerReqDto.getPhoneNumber())
                .password(passwordEncoder.encode(registerReqDto.getPassword()))
                .role(Role.NORMAL)
                .build();
        Member savedMember = memberRepository.save(registerMember);

        return savedMember.getPhoneNumber();
    }

    public void changeToManager(Long memberId, AgencyDto agencyDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원정보가 존재하지 않습니다."));

        // 이미 관리자일 때 예외처리
        if (member.getRole() == Role.MANAGER) {
            throw new IllegalArgumentException("invalid / 회원정보 수정에서 처리돼야함");
        }
        // agency가 이미 존재할 때, 기존 것에 연결처리
        Optional<Agency> sameAgency = agencyRepository.findByAddressAndName(agencyDto.getAddress(), agencyDto.getName());
//        Agency agency = sameAgency.orElseGet(()
//                -> agencyRepository.save(agencyDto.toEntity()));
        Agency agency = sameAgency.isPresent() ? sameAgency.get() : agencyRepository.save(agencyDto.toEntity());

        member.setAgencyAndRole(agency, Role.MANAGER);
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        Member member = memberRepository.findByPhoneNumber(loginReqDto.getPhoneNumber())
                        .orElseThrow(() -> new UsernameNotFoundException("회원정보가 존재하지 않습니다."));
        MemberDto memberDto = MemberDto.of(member);

        verifyPassword(loginReqDto.getPassword(), memberDto);

        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setMember(memberDto);
        makeTokens(memberDto, loginResDto);
        return loginResDto;
    }

    public void logout(Long memberId) {
        tokenRepository.deleteRefreshToken(memberId);
    }

    public boolean checkDuplicate(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    public FindMemberResDto findById(Long targetMemberId, Long requestMemberId) {

        Member member = memberRepository.findByIdWithAgency(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원정보가 존재하지 않습니다."));

//        System.out.println("ddd : " + member.getAgency().getId());


        validMemberNotDeleted(member);

        FindMemberResDto foundMember = FindMemberResDto.of(member);

        if (!foundMember.getMemberId().equals(requestMemberId)) {
            foundMember.setPhoneNumber(blindPhoneNumber(foundMember.getPhoneNumber()));
        }
        return foundMember;
    }

    /**
     * 입력 가능한 파라미터들 값을 미리 내려준 후, 모두 들어있는 값을 받는다.
     * @return
     */
    public BriefMemberDto modifyMember(Long memberId, ModifyMemberReqDto modifyMemberReqDto) {

        System.out.println("아아아" + modifyMemberReqDto.toString());
        if (!memberId.equals(modifyMemberReqDto.getMemberId())) {
            throw new RuntimeException("다른 유저 정보수정; 403 처리");
        }
        Member member = memberRepository.findById(modifyMemberReqDto.getMemberId())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 접근입니다."));
        validMemberNotDeleted(member);

        // update - 항상 내려 주고, 전부 받는 게 자연스럽다.
        // 케이스별 구분, Agency 처리
        if (member.getRole() == Role.MANAGER) {
            if (modifyMemberReqDto.getRole() == Role.MANAGER) {
                managerToManager(member, modifyMemberReqDto);
            } else if (modifyMemberReqDto.getRole() == Role.NORMAL) {
                managerToNormal(member, modifyMemberReqDto);
            }
        } else if (member.getRole() == Role.NORMAL) {
            if (modifyMemberReqDto.getRole() == Role.MANAGER) {
                normalToManager(member, modifyMemberReqDto);
            }
        }
        // 나머지 member 정보 변경 : 일단은 전화번호만
        member.changePhoneNumber(modifyMemberReqDto.getPhoneNumber());
        return new BriefMemberDto(member.getId(), member.getPhoneNumber());
    }

    public void deleteMember(Long requestMemberId, Long targetMemberId) {
        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 접근입니다."));
        validMemberNotDeleted(member);
        if (!targetMemberId.equals(requestMemberId)) {
            throw new AuthenticationServiceException("권한이 없습니다.");
        }
        member.withdraw();
    }

//    public List<Member> findMembers(String keyword) {
//        List<Member> members = memberRepository.findAll();
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

        Member member = memberRepository.findById(memberId)
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationServiceException("401 unauthorized"));
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

    private void validMemberNotDeleted(Member member) {
        if (member.getWithdrawalYn() != null && !member.getWithdrawalYn()) {
            throw new UsernameNotFoundException("회원정보가 존재하지 않습니다.");
        }
    }

    private Agency saveAgency(Agency agency) {
        Optional<Agency> optionalAgency = agencyRepository.findByAddressAndName(agency.getAddress(),
                agency.getName());

        Agency savedAgency = optionalAgency.isPresent() ? optionalAgency.get() : agencyRepository.save(agency);

        return savedAgency;
//        return optionalAgency.orElseGet(() -> agencyRepository.save(agency));
    }

    private void normalToManager(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        Agency agency = saveAgency(modifyReqDto.getAgency().toEntity());
        oldMember.setAgencyAndRole(agency, Role.MANAGER);
    }

    private void managerToManager(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        Agency agency = saveAgency(modifyReqDto.getAgency().toEntity());
        oldMember.changeAgency(agency);
    }

    private void normalToNormal(Member oldMember, ModifyMemberReqDto modifyReqDto) {

    }

    private void managerToNormal(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        oldMember.toNormal();
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
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    private String blindPhoneNumber(String phoneNumber) {
        int len = phoneNumber.length();
        if (len <= 4) return phoneNumber;
        return "****" + phoneNumber.substring(len - 4);
    }
}
