package com.cmips.repository;

import com.cmips.entity.ModeOfServiceSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModeOfServiceSnapshotRepository extends JpaRepository<ModeOfServiceSnapshotEntity, Long> {
    List<ModeOfServiceSnapshotEntity> findByModeOfServiceIdOrderBySnapshotDateDesc(Long mosId);
}
