package com.findear.main.member.command.dto;

import lombok.Getter;

@Getter
public class BriefMemberDto {
    private Long memberId;
    private String phoneNumber;

    public BriefMemberDto(Long memberId, String phoneNumber) {
        this.memberId = memberId;
        this.phoneNumber = phoneNumber;
    }
}
