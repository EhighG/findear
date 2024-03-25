package com.findear.main.board.query.repository;

import com.findear.main.board.common.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LostBoardQueryRepository extends JpaRepository<Board, Long> {

}
