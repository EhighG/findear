package com.findear.main.member.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class LoginReqDto {

    private String phoneNumber;
    private String password;
}
