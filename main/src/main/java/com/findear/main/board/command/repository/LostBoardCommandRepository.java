package com.findear.main.board.command.repository;

import com.findear.main.board.common.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LostCommandRepository extends JpaRepository<Board, Long> {
}
