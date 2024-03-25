package com.findear.main.board.query.repository;

import com.findear.main.board.common.domain.AcquiredBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcquiredBoardQueryRepository extends JpaRepository<AcquiredBoard, Long> {

    @Query("select ab from AcquiredBoard ab join fetch ab.board left join fetch ab.board.imgFileList where ab.board.id = :boardId and ab.board.deleteYn is null and ab.board.color is not null")
    Optional<AcquiredBoard> findByBoardId(Long boardId);

    // delete_yn 기본값 적용 시 where 조건 바꾸기
    @Query("select ab from AcquiredBoard ab join fetch ab.board left join fetch ab.board.imgFileList where ab.board.deleteYn is null and ab.board.color is null")
    List<AcquiredBoard> findAll();
}
