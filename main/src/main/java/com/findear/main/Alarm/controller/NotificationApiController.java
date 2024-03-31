package com.findear.main.Alarm.controller;

import com.findear.main.Alarm.service.NotificationService;
import com.findear.main.member.query.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationApiController {

    private final NotificationService notificationService;

    @PostMapping("/new")
    public void saveNotification(String token) {

        Long memberId = MemberQueryService.getAuthenticatedMemberId();

        notificationService.saveNotification(token, memberId);
    }

}