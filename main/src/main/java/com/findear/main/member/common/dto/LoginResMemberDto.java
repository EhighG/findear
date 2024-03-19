package com.findear.main.member.common.dto;

import lombok.Getter;

@Getter
public class LoginResMemberDto {
    private Long memberId;
    private String phoneNumber;

    public LoginResMemberDto(Long memberId, String phoneNumber) {
        this.memberId = memberId;
        this.phoneNumber = phoneNumber;
    }
}
