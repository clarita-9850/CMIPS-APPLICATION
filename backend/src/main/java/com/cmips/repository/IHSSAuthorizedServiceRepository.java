package com.cmips.repository;

import com.cmips.entity.IHSSAuthorizedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IHSSAuthorizedServiceRepository extends JpaRepository<IHSSAuthorizedServiceEntity, Long> {
    List<IHSSAuthorizedServiceEntity> findByIhssAuthorizationId(Long ihssAuthorizationId);
    List<IHSSAuthorizedServiceEntity> findByIhssAuthorizationIdAndServiceTypeCode(Long authId, String serviceTypeCode);
}
