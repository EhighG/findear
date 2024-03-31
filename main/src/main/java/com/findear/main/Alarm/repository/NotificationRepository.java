package com.findear.main.Alarm.repository;

import com.findear.main.Alarm.common.domain.Notification;
import com.findear.main.member.common.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByMember(Member member);
}