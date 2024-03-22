package com.findear.main.member.common.repository;

import com.findear.main.member.common.domain.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    @Query("select a from Agency a left join fetch a.memberList where a.address = :address and a.name = :name")
    Optional<Agency> findByAddressAndName(String address, String name);

}
