package com.cmips.service;

import com.cmips.entity.CCInvoiceDetailsEntity;
import com.cmips.entity.CountyContractorEntity;
import com.cmips.entity.CountyContractorInvoiceEntity;
import com.cmips.repository.CCInvoiceDetailsRepository;
import com.cmips.repository.CountyContractorInvoiceRepository;
import com.cmips.repository.CountyContractorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * County Contractor Service
 * Implements business rules from DSD Sections 20-21 for County Contractor
 * Rate Management and Invoice Processing.
 */
@Service
public class CountyContractorService {

    private static final Logger log = LoggerFactory.getLogger(CountyContractorService.class);

    private final CountyContractorRepository contractorRepository;
    private final CountyContractorInvoiceRepository invoiceRepository;
    private final CCInvoiceDetailsRepository detailsRepository;

    public CountyContractorService(
            CountyContractorRepository contractorRepository,
            CountyContractorInvoiceRepository invoiceRepository,
            CCInvoiceDetailsRepository detailsRepository) {
        this.contractorRepository = contractorRepository;
        this.invoiceRepository = invoiceRepository;
        this.detailsRepository = detailsRepository;
    }

    // ==================== RATE CRUD ====================

    /**
     * Get all rates for a county, ordered by effective date descending.
     */
    public List<CountyContractorEntity> getRatesByCounty(String countyCode) {
        return contractorRepository.findByCountyCodeOrderByFromDateDesc(countyCode);
    }

    /**
     * Get a single rate by ID.
     */
    public CountyContractorEntity getRateById(Long id) {
        return contractorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("County Contractor rate not found with id: " + id));
    }

    /**
     * Create a new rate with DSD validation.
     * Validates: EM 13 (county required), EM 14 (effective date = 1st of month),
     * EM 15 (rate >= wage), EM 16 (no overlap), EM 17 (effective <= end),
     * EM 18 (end date = last of month)
     */
    @Transactional
    public CountyContractorEntity createRate(CountyContractorEntity entity, String userId) {
        validateRate(entity, 0L);
        entity.setCreatedBy(userId);
        log.info("Creating county contractor rate for county={}, contractor={}", entity.getCountyCode(), entity.getContractorName());
        return contractorRepository.save(entity);
    }

    /**
     * Modify an existing rate with DSD validation.
     * Per DSD, contractorName is read-only on modify screen.
     */
    @Transactional
    public CountyContractorEntity modifyRate(Long id, CountyContractorEntity updates, String userId) {
        CountyContractorEntity existing = getRateById(id);
        validateRate(updates, id);

        // Update only editable fields per DSD Modify screen
        existing.setFromDate(updates.getFromDate());
        existing.setToDate(updates.getToDate());
        existing.setRateAmt(updates.getRateAmt());
        existing.setWageAmt(updates.getWageAmt());
        existing.setMacrAmt(updates.getMacrAmt());
        existing.setUpdatedBy(userId);

        log.info("Modifying county contractor rate id={}", id);
        return contractorRepository.save(existing);
    }

    /**
     * Validate rate per DSD error messages.
     */
    private void validateRate(CountyContractorEntity entity, Long excludeId) {
        List<String> errors = new ArrayList<>();

        // EM 13: County is required
        if (entity.getCountyCode() == null || entity.getCountyCode().isBlank()) {
            errors.add("County is required");
        }

        // EM 14: Effective Date must be the first day of a month
        if (entity.getFromDate() != null && entity.getFromDate().getDayOfMonth() != 1) {
            errors.add("Effective Date must be the first day of a month");
        }

        // EM 18: End Date must be the last day of a month
        if (entity.getToDate() != null) {
            YearMonth ym = YearMonth.from(entity.getToDate());
            if (entity.getToDate().getDayOfMonth() != ym.lengthOfMonth()) {
                errors.add("End Date must be the last day of a month");
            }
        }

        // EM 17: Effective Date must be on or before End Date
        if (entity.getFromDate() != null && entity.getToDate() != null
                && entity.getFromDate().isAfter(entity.getToDate())) {
            errors.add("Effective Date must be on or before End Date");
        }

        // EM 15: Rate must be >= Wage
        if (entity.getRateAmt() != null && entity.getWageAmt() != null
                && entity.getRateAmt().compareTo(entity.getWageAmt()) < 0) {
            errors.add("Rate must be greater than or equal to Wage");
        }

        // EM 16: Date range overlaps with existing rate
        if (entity.getCountyCode() != null && entity.getContractorName() != null
                && entity.getFromDate() != null) {
            LocalDate toDate = entity.getToDate() != null ? entity.getToDate() : LocalDate.of(9999, 12, 31);
            List<CountyContractorEntity> overlapping = contractorRepository.findOverlappingRates(
                    entity.getCountyCode(), entity.getContractorName(),
                    entity.getFromDate(), toDate, excludeId);
            if (!overlapping.isEmpty()) {
                errors.add("Date range overlaps with existing rate");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    // ==================== INVOICE OPERATIONS ====================

    /**
     * Get all invoices for a county contractor.
     */
    public List<CountyContractorInvoiceEntity> getInvoicesByContractor(Long countyContractorId) {
        return invoiceRepository.findByCountyContractorIdOrderByBillingMonthDesc(countyContractorId);
    }

    /**
     * Get a single invoice by ID.
     */
    public CountyContractorInvoiceEntity getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("County Contractor Invoice not found with id: " + id));
    }

    /**
     * Modify invoice — only warrant number and paid date are editable.
     * Validates: EM 12 (warrant requires paid date), EM 13 (paid date requires warrant),
     * EM 14 (paid date > processed date).
     * BR #37: When saved with warrant + paid date, status becomes Paid.
     */
    @Transactional
    public CountyContractorInvoiceEntity modifyInvoice(Long id, String warrantNumber, LocalDate paidDate, String userId) {
        CountyContractorInvoiceEntity invoice = getInvoiceById(id);
        List<String> errors = new ArrayList<>();

        boolean hasWarrant = warrantNumber != null && !warrantNumber.isBlank();
        boolean hasPaidDate = paidDate != null;

        // EM 12: Paid Date is required when a warrant number is entered
        if (hasWarrant && !hasPaidDate) {
            errors.add("Paid Date is required when a warrant number is entered");
        }

        // EM 13: Warrant number is required when the Paid Date is entered
        if (hasPaidDate && !hasWarrant) {
            errors.add("Warrant number is required when the Paid Date is entered");
        }

        // EM 14: Paid Date must be later than Processed Date
        if (hasPaidDate && invoice.getProcessedDate() != null && paidDate.isBefore(invoice.getProcessedDate())) {
            errors.add("Paid Date must be later than Processed Date");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        invoice.setWarrantNumber(warrantNumber);
        invoice.setPaidDate(paidDate);
        invoice.setUpdatedBy(userId);

        // BR #37: Set status to Paid when both warrant and paid date are present
        if (hasWarrant && hasPaidDate) {
            invoice.setStatus("Paid");
            log.info("Invoice id={} status changed to Paid (BR #37)", id);
        }

        return invoiceRepository.save(invoice);
    }

    /**
     * Get invoice detail rows for an invoice.
     */
    public List<CCInvoiceDetailsEntity> getInvoiceDetails(Long invoiceId) {
        return detailsRepository.findByCountyContractorInvoiceId(invoiceId);
    }

    /**
     * Validate SOC 432 report prerequisites (DSD EM 15 for View CC Invoice).
     * Both warrant number and paid date must be present.
     */
    public CountyContractorInvoiceEntity validateAndGetSoc432(Long invoiceId) {
        CountyContractorInvoiceEntity invoice = getInvoiceById(invoiceId);
        boolean hasWarrant = invoice.getWarrantNumber() != null && !invoice.getWarrantNumber().isBlank();
        boolean hasPaidDate = invoice.getPaidDate() != null;

        if (!hasWarrant || !hasPaidDate) {
            throw new IllegalArgumentException(
                    "The warrant number and the Paid Date are both required to produce the Contract Expenditure Report");
        }
        return invoice;
    }
}
