//package com.findear.main.board.query.repository;
//
//import com.findear.main.board.common.domain.ReturnLog;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface ReturnLogRepository extends JpaRepository<ReturnLog, Long> {
//
//    @Query("select count(*) from ReturnLog rl where rl.cancelAt is null and function('DATE', rl.returnedAt) = current date - 1")
//    Long countYesterdaysReturn();
//
////    Optional<ReturnLog> findById(Long id);
//}
