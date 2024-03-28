package com.findear.main.message.command.service;

import com.findear.main.board.command.repository.BoardCommandRepository;
import com.findear.main.board.common.domain.Board;
import com.findear.main.board.query.repository.LostBoardQueryRepository;
import com.findear.main.member.common.domain.Member;
import com.findear.main.member.query.repository.MemberQueryRepository;
import com.findear.main.message.command.dto.SendMessageReqDto;
import com.findear.main.message.command.repository.MessageCommandRepository;
import com.findear.main.message.command.repository.MessageRoomCommandRepository;
import com.findear.main.message.common.domain.Message;
import com.findear.main.message.common.domain.MessageRoom;
import com.findear.main.message.common.exception.MessageException;
import com.findear.main.message.query.repository.MessageRoomQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MessageCommandService {

    private final MessageCommandRepository messageCommandRepository;
    private final BoardCommandRepository boardQueryRepository;
    private final MessageRoomCommandRepository messageRoomCommandRepository;
    private final MessageRoomQueryRepository messageRoomQueryRepository;
    private final MemberQueryRepository memberQueryRepository;

    public void sendMessage(SendMessageReqDto sendMessageReqDto) {

        try {

            MessageRoom findMessageRoom = messageRoomQueryRepository.findByBoardIdAndMemberId(sendMessageReqDto.getBoardId(), sendMessageReqDto.getMemberId());

            // 쪽지방이 생성되지 않은 상태면 쪽지방 생성
            if(findMessageRoom == null) {

                Member findMember = memberQueryRepository.findById(sendMessageReqDto.getMemberId())
                        .orElseThrow(() -> new MessageException("해당 유저가 없습니다."));

                Board findBoard = boardQueryRepository.findById(sendMessageReqDto.getBoardId())
                        .orElseThrow(() -> new MessageException("해당 게시판이 존재하지 않습니다."));

                findMessageRoom = MessageRoom.builder()
                        .member(findMember).board(findBoard).build();

                messageRoomCommandRepository.save(findMessageRoom);
            }

            // 메세지 생성
            Message newMessage = Message.builder()
                    .messageRoom(findMessageRoom)
                    .title(sendMessageReqDto.getTitle())
                    .senderId(sendMessageReqDto.getMemberId())
                    .content(sendMessageReqDto.getContent())
                    .sendAt(LocalDateTime.now()).build();

            messageCommandRepository.save(newMessage);

        }
        catch (Exception e) {

            throw new MessageException(e.getMessage());
        }
    }
}
