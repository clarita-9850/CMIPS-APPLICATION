package com.cmips.repository;

import com.cmips.entity.ProviderNotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderNotificationPreferenceRepository extends JpaRepository<ProviderNotificationPreferenceEntity, Long> {
    Optional<ProviderNotificationPreferenceEntity> findByProviderId(Long providerId);
}
