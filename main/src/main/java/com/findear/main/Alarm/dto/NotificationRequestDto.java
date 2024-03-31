package com.findear.main.Alarm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequestDto {

    private String title;
    private String message;
    private Long memberId;

    @Builder
    public NotificationRequestDto(String title, String message, Long memberId) {
        this.title = title;
        this.message = message;
        this.memberId = memberId;
    }
}