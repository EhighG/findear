package com.findear.main.board.command.repository;

import com.findear.main.board.common.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
}
