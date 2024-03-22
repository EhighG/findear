package com.findear.main.Alarm.service;

import com.findear.main.Alarm.common.domain.Alarm;
import com.findear.main.Alarm.common.exception.AlarmException;
import com.findear.main.Alarm.dto.AlarmDataDto;
import com.findear.main.Alarm.dto.ShowAlarmDto;
import com.findear.main.Alarm.repository.AlarmRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public List<AlarmDataDto> showAlarmList(Long memberId) {

        try {

            List<Alarm> findAlarms = alarmRepository.findAllByMemberId(memberId);

            List<AlarmDataDto> results = new ArrayList<>();
            for(Alarm a : findAlarms) {

                results.add(AlarmDataDto.builder()
                        .alarmId(a.getId())
                        .author(a.getAuthor())
                        .content(a.getContent())
                        .generatedAt(a.getGeneratedAt())
                        .readYn(a.getReadYn())
                        .memberId(a.getMember().getId()).build());
            }

            return results;
        } catch (Exception e) {
            throw new AlarmException(e.getMessage());
        }
    }

    public AlarmDataDto showAlarm(ShowAlarmDto showAlarmDto) {

        try {

            Alarm findAlarm = alarmRepository.findByAlarmIdAndMemberId(showAlarmDto.getAlarmId(), showAlarmDto.getMemberId());

            findAlarm.readAlarm();

            AlarmDataDto result = AlarmDataDto.builder()
                    .alarmId(findAlarm.getId())
                    .memberId(findAlarm.getMember().getId())
                    .author(findAlarm.getAuthor())
                    .content(findAlarm.getContent())
                    .readYn(findAlarm.getReadYn())
                    .generatedAt(findAlarm.getGeneratedAt()).build();

            return result;

        } catch (Exception e) {
            throw new AlarmException(e.getMessage());
        }

    }
}
