package com.findear.main.member.command.service;

import com.findear.main.member.command.dto.*;
import com.findear.main.member.command.repository.AgencyCommandRepository;
import com.findear.main.member.common.domain.Agency;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.domain.Role;
import com.findear.main.member.common.dto.*;
import com.findear.main.member.query.repository.AgencyQueryRepository;
import com.findear.main.member.command.repository.MemberCommandRepository;
import com.findear.main.member.query.service.MemberQueryService;
import com.findear.main.security.RefreshTokenRepository;
import com.findear.main.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberCommandRepository memberCommandRepository;
    private final AgencyQueryRepository agencyQueryRepository;
    private final AgencyCommandRepository agencyCommandRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * @param registerReqDto - phoneNumber, password
     * @return phoneNumber
     * @throws IllegalArgumentException - phoneNumber 중복
     */
    public String register(RegisterReqDto registerReqDto) {
        if (memberQueryService.checkDuplicate(registerReqDto.getPhoneNumber())) {
            throw new IllegalArgumentException("회원정보가 이미 있습니다.");
        }
        Member registerMember = Member.builder()
                .phoneNumber(registerReqDto.getPhoneNumber())
                .password(passwordEncoder.encode(registerReqDto.getPassword()))
                .role(Role.NORMAL)
                .build();
        Member savedMember = memberCommandRepository.save(registerMember);

        return savedMember.getPhoneNumber();
    }

    public void changeToManager(Long memberId, AgencyDto agencyDto) {
        Member member = memberQueryService.internalFindById(memberId);

        // 이미 관리자일 때 예외처리
        if (member.getRole() == Role.MANAGER) {
            throw new IllegalArgumentException("invalid / 회원정보 수정에서 처리돼야함");
        }
        // agency가 이미 존재할 때, 기존 것에 연결처리
        Optional<Agency> sameAgency = agencyQueryRepository.findByAddressAndName(agencyDto.getAddress(), agencyDto.getName());

        Agency agency = sameAgency.isPresent() ? sameAgency.get() : agencyCommandRepository.save(agencyDto.toEntity());

        member.setAgencyAndRole(agency, Role.MANAGER);
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        MemberDto memberDto = memberQueryService.findByPhoneNumber(loginReqDto.getPhoneNumber());

        verifyPassword(loginReqDto.getPassword(), memberDto);

        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setMemberAndAgency(memberDto);
        makeTokens(memberDto, loginResDto);
        return loginResDto;
    }

    public void logout(Long memberId) {
        tokenRepository.deleteRefreshToken(memberId);
    }

    /**
     * 입력 가능한 파라미터들 값을 미리 내려준 후, 모두 들어있는 값을 받는다.
     * @return
     */
    public BriefMemberDto modifyMember(Long memberId, ModifyMemberReqDto modifyMemberReqDto) {
        if (!memberId.equals(modifyMemberReqDto.getMemberId())) {
            throw new RuntimeException("다른 유저 정보수정; 403 처리");
        }
        Member member = memberQueryService.internalFindById(modifyMemberReqDto.getMemberId());

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
        return new BriefMemberDto(member.getId(), member.getPhoneNumber(), member.getRole());
    }

    public void deleteMember(Long requestMemberId, Long targetMemberId) {
        Member member = memberQueryService.internalFindById(targetMemberId);
        if (!targetMemberId.equals(requestMemberId)) {
            throw new AuthenticationServiceException("권한이 없습니다.");
        }
        member.withdraw();
    }

//    public List<Member> findMembers(String keyword) {
//        List<Member> members = memberRepository.findAll();
//        return members.stream().filter((member) -> member.getPhoneNumber().contains(keyword)).collect(Collectors.toList());
//    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        // refreshToken 검증은 마친 상태
        Long memberId = jwtService.getMemberId(refreshToken);
        String accessToken = jwtService.createAccessToken(memberId);
        Map<String, String> result = new HashMap<>();
        result.put("tokenType", JwtService.TOKEN_TYPE);
        result.put("accessToken", accessToken);
        return result;
    }

    private Agency saveAgency(Agency agency) {
        Optional<Agency> optionalAgency = agencyQueryRepository.findByAddressAndName(agency.getAddress(),
                agency.getName());

        Agency savedAgency = optionalAgency.isPresent() ? optionalAgency.get() : agencyQueryRepository.save(agency);

        return savedAgency;
    }

    private void normalToManager(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        Agency agency = saveAgency(modifyReqDto.getAgency().toEntity());
        oldMember.setAgencyAndRole(agency, Role.MANAGER);
    }

    private void managerToManager(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        Agency agency = saveAgency(modifyReqDto.getAgency().toEntity());
        oldMember.changeAgency(agency);
    }

    private void managerToNormal(Member oldMember, ModifyMemberReqDto modifyReqDto) {
        oldMember.toNormal();
    }


    private void verifyPassword(String password, MemberDto member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    private LoginResDto makeTokens(MemberDto memberDto, LoginResDto loginResDto) {
        String accessToken = jwtService.createAccessToken(memberDto.getId());
        String refreshToken = jwtService.createRefreshToken(memberDto.getId());
        tokenRepository.save(memberDto.getId(), refreshToken);

        loginResDto.setAccessToken(accessToken);
        loginResDto.setRefreshToken(refreshToken);
        return loginResDto;
    }
}
