package com.findear.main.Alarm.service;

import com.findear.main.Alarm.common.domain.Notification;
import com.findear.main.Alarm.common.exception.AlarmException;
import com.findear.main.Alarm.dto.NotificationRequestDto;
import com.findear.main.Alarm.dto.SaveNotificationReqDto;
import com.findear.main.Alarm.repository.NotificationRepository;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.query.repository.MemberQueryRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.firebase.messaging.Message;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberQueryRepository memberQueryRepository;

    @Transactional
    public void saveNotification(SaveNotificationReqDto saveNotificationReqDto) {

        Member findMember = memberQueryRepository.findById(saveNotificationReqDto.getMemberId())
                .orElseThrow(() -> new AlarmException("해당 유저가 존재하지 않습니다."));

        Notification newNotification = Notification.builder()
                .token(saveNotificationReqDto.getToken())
                .build();

        newNotification.confirmUser(findMember);

        notificationRepository.save(newNotification);
    }

    public void sendNotification(NotificationRequestDto req) {

        try {

            log.info("제목 : " + req.getTitle());
            log.info("메시지 : " + req.getMessage());
            log.info("메시지 타입 : " + req.getType());
            log.info("토큰 : " + getNotificationToken(req.getMemberId()));

            Message message = Message.builder()
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(req.getTitle())
                                    .setBody(req.getMessage())
                                    .build())
                            .build())
                    .setToken(getNotificationToken(req.getMemberId()))
                    .build();

            String response = FirebaseMessaging.getInstance().sendAsync(message).get();
            log.info(">>>>Send message : " + response);
        } catch (Exception e) {
            throw new AlarmException(e.getMessage());
        }
    }

    public String getNotificationToken(Long memberId) {

        try {

            Member findMember = memberQueryRepository.findById(memberId)
                    .orElseThrow(() -> new AlarmException("해당 유저가 존재하지 않습니다."));

            Notification notification = notificationRepository.findByMember(findMember)
                    .orElseThrow(() -> new AlarmException("해당 유저가 존재하지 않습니다."));

            return notification.getToken();

        } catch (Exception e) {
            throw new AlarmException(e.getMessage());
        }
    }

    public void deleteNotification(Long memberId) {

        try {

            Member findMember = memberQueryRepository.findById(memberId)
                    .orElseThrow(() -> new AlarmException("해당 유저가 존재하지 않습니다."));

            Notification notification = notificationRepository.findByMemberId(findMember.getId());

            if(notification != null) {
                notificationRepository.delete(notification);
            }

        } catch (Exception e) {
            throw new AlarmException(e.getMessage());
        }
    }
}