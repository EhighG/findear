package com.findear.main.member.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginResDto {

    @Setter
    private String accessToken;

    @Setter
    private String refreshToken;

    private LoginResMemberDto member;


    public void setMember(MemberDto memberDto) {
        this.member = new LoginResMemberDto(memberDto.getId(), memberDto.getPhoneNumber());
    }
}
