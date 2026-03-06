package com.cmips.repository;

import com.cmips.entity.NoaCategoryMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoaCategoryMessageRepository extends JpaRepository<NoaCategoryMessageEntity, Long> {

    Optional<NoaCategoryMessageEntity> findByCategoryCode(String categoryCode);

    List<NoaCategoryMessageEntity> findByCategoryGroupOrderByCategoryCode(String categoryGroup);
}
