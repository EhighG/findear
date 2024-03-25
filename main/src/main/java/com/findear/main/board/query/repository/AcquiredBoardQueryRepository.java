package com.findear.main.board.query.repository;

import com.findear.main.board.common.domain.AcquiredBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcquiredBoardQueryRepository extends JpaRepository<AcquiredBoard, Long> {
}
