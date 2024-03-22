package com.findear.main.message.query.repository;

import com.findear.main.message.common.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRoomQueryRepository extends JpaRepository<MessageRoom, Long> {


    @Query("select mr from MessageRoom mr where mr.board.id = :boardId and mr.member.id = :memberId")
    MessageRoom findByBoardIdAndMemberId(Long boardId, Long memberId);
}
