package com.findear.main.board.query.repository;

import com.findear.main.board.common.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardQueryRepository extends JpaRepository<Board, Long> {
}
