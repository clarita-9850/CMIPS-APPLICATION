package com.cmips.repository;

import com.cmips.entity.ElectronicFormEntity;
import com.cmips.entity.ElectronicFormEntity.FormStatus;
import com.cmips.entity.ElectronicFormEntity.PrintMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectronicFormRepository extends JpaRepository<ElectronicFormEntity, Long> {

    List<ElectronicFormEntity> findByCaseIdOrderByRequestDateDesc(Long caseId);

    List<ElectronicFormEntity> findByCaseIdAndStatusOrderByRequestDateDesc(Long caseId, FormStatus status);

    /** For nightly batch processing — all PENDING forms that use NIGHTLY_BATCH print method */
    List<ElectronicFormEntity> findByStatusAndPrintMethod(FormStatus status, PrintMethod printMethod);
}
