package com.findear.main.member.common.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_email_verify")
public class EmailVerify {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verify_id")
    private Long id;

    private String email;

    private String verifyCode;

    private Boolean verifyYn;

    private LocalDateTime expireAt;
}
