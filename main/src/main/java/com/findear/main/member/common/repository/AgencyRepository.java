package com.findear.main.member.common.repository;

import com.findear.main.member.common.domain.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    Optional<Agency> findByAddressAndName(String address, String name);
}
