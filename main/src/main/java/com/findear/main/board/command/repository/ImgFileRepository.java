package com.findear.main.board.command.repository;

import com.findear.main.board.common.domain.ImgFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImgFileRepository extends JpaRepository<ImgFile, Long> {
}
