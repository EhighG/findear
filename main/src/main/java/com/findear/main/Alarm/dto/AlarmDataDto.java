package com.findear.main.Alarm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class AlarmDataDto {

    private Long alarmId;

    private Long memberId;

    private String author;

    private String content;

    private LocalDateTime generatedAt;

    private Boolean readYn;
}
