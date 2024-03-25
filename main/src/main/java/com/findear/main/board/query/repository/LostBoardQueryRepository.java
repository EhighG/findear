package com.findear.main.board.query.repository;

import com.findear.main.board.common.domain.Board;
import com.findear.main.board.common.domain.LostBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LostBoardQueryRepository extends JpaRepository<LostBoard, Long> {

//    @Query("select lb from LostBoard lb join fetch Board b where lb.board.id = :boardId")
    Optional<LostBoard> findByBoardId(Long boardId);

    // delete_yn 기본값 적용 시 where 조건 바꾸기
    @Query("select lb from LostBoard lb join fetch lb.board left join fetch lb.board.imgFileList where lb.board.deleteYn is null")
    List<LostBoard> findAll();
}
