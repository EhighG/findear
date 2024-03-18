package com.findear.main.member.common.dto;

import com.findear.main.member.common.domain.Role;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterReqDto {

    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;
    private Role role;
}
