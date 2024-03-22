package com.findear.main.member.command.repository;

import com.findear.main.member.common.domain.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AgencyCommandRepository extends JpaRepository<Agency, Long> {

}
