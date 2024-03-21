package com.findear.main.member.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginResDto {

    @Setter
    private String accessToken;

    @Setter
    private String refreshToken;

    private BriefMemberDto member;


    public void setMember(MemberDto memberDto) {
        this.member = new BriefMemberDto(memberDto.getId(), memberDto.getPhoneNumber());
    }
}
