package com.cmips.repository;

import com.cmips.entity.TaxContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxContributionRepository extends JpaRepository<TaxContributionEntity, Long> {

    List<TaxContributionEntity> findByProviderIdOrderByTaxYearDescTaxQuarterDesc(Long providerId);

    List<TaxContributionEntity> findByProviderIdAndTaxYear(Long providerId, Integer taxYear);

    Optional<TaxContributionEntity> findByProviderIdAndTaxYearAndTaxQuarter(
            Long providerId, Integer taxYear, Integer taxQuarter);

    List<TaxContributionEntity> findByTaxYearAndW2Generated(Integer taxYear, Boolean w2Generated);
}
