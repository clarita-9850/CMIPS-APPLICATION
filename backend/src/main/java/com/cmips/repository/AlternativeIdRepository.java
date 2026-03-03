package com.cmips.repository;

import com.cmips.entity.AlternativeIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlternativeIdRepository extends JpaRepository<AlternativeIdEntity, Long> {

    List<AlternativeIdEntity> findByPersonIdAndPersonType(Long personId, String personType);

    List<AlternativeIdEntity> findByOriginalSsn(String originalSsn);

    List<AlternativeIdEntity> findByMasterCin(String masterCin);
}
