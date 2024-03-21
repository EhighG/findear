package com.findear.main.member.common.dto;

import com.findear.main.member.common.domain.Agency;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.common.domain.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FindMemberResDto {
    private Long memberId;
    @Setter
    private String phoneNumber;
    private Role role;
    private LocalDateTime joinedAt;
    private Agency agency;

    public static FindMemberResDto of(Member member) {
        return FindMemberResDto.builder()
                .memberId(member.getId())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .agency(member.getAgency())
                .build();
    }
}
