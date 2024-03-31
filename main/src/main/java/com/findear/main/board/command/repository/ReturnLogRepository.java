package com.findear.main.board.command.repository;

import com.findear.main.board.common.domain.AcquiredBoard;
import com.findear.main.board.common.domain.ReturnLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnLogRepository extends JpaRepository<ReturnLog, Long> {

    Optional<ReturnLog> findFirstByAcquiredBoardAndCancelAtIsNullOrderByIdDesc(AcquiredBoard acquiredBoard);
}
