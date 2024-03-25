package com.findear.main.message.query.service;


import com.findear.main.message.common.domain.MessageRoom;
import com.findear.main.message.common.exception.MessageException;
import com.findear.main.message.query.dto.ShowMessageListReqDto;
import com.findear.main.message.query.dto.ShowMessageListResDto;
import com.findear.main.message.query.dto.ShowMessageRoomDetailReqDto;
import com.findear.main.message.query.repository.MessageRoomQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MessageQueryService {

    private final MessageRoomQueryRepository messageRoomQueryRepository;

    public List<ShowMessageListResDto> showMessageRoomList(ShowMessageListReqDto showMessageListReqDto) {

        try {

            List<MessageRoom> messageRoomList = messageRoomQueryRepository.findAllByMemberIdWithBoardAndMessage(showMessageListReqDto.getMemberId());

            List<ShowMessageListResDto> result = messageRoomList.stream()
                    .map(mr -> ShowMessageListResDto.builder()
                            .messageRoomId(mr.getId())
                            .boardId(mr.getBoard().getId())
                            .thumbnailUrl(mr.getBoard().getThumbnailUrl())
                            .productName(mr.getBoard().getProductName())
                            .description(mr.getBoard().getDescription())
                            .title(mr.getMessageList().get(mr.getMessageList().size()).getTitle())
                            .content(mr.getMessageList().get(mr.getMessageList().size()).getContent())
                            .sendAt(mr.getMessageList().get(mr.getMessageList().size()).getSendAt()).build()).toList();

            return result;

        } catch (Exception e) {

            throw new MessageException(e.getMessage());
        }
    }

    public List<ShowMessageListResDto> showMessageRoomDetail(ShowMessageRoomDetailReqDto showMessageListReqDto) {

        try {

            return null;
        } catch (Exception e) {
            throw new MessageException(e.getMessage());
        }
    }
}
