package com.cmips.repository;

import com.cmips.entity.EmailTemplateEntity;
import com.cmips.entity.EmailTemplateEntity.TemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, Long> {

    Optional<EmailTemplateEntity> findByTemplateCode(String templateCode);

    List<EmailTemplateEntity> findByCategoryAndActiveTrue(TemplateCategory category);

    List<EmailTemplateEntity> findByActiveTrue();
}
