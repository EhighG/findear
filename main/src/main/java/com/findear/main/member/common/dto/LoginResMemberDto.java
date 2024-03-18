package com.findear.main.member.common.dto;

import lombok.Getter;

@Getter
public class LoginResMemberDto {
    private Long memberId;
    private String nickname;

    public LoginResMemberDto(Long memberId, String nickname) {
        this.memberId = memberId;
        this.nickname = nickname;
    }
}
