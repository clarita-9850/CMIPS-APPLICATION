package com.cmips.entity;

import jakarta.persistence.*;

/**
 * NOA Category Message — DSD Appendix G
 *
 * Each row holds one category code (e.g. "DN01", "HR02") with its title
 * and the approved translated text for all 4 supported languages.
 * Variable placeholders use {VAR} syntax:
 *   {DATE}     — effective date (MM/DD/YYYY)
 *   {HOURS}    — authorized hours (HHH:MM)
 *   {PERCENT}  — percentage (XX.X%)
 *   {SERVICES} — comma-delimited list of services
 *   {COUNTY}   — county name
 *   {AMOUNT}   — dollar amount
 *   {MONTH}    — month name
 *   {YEAR}     — four-digit year
 */
@Entity
@Table(name = "noa_category_messages",
        uniqueConstraints = @UniqueConstraint(columnNames = "category_code"))
public class NoaCategoryMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "DN01", "HR02", "AA03" */
    @Column(name = "category_code", nullable = false, length = 10)
    private String categoryCode;

    /** Two-letter group prefix: "DN", "HR", "AA", "FS", etc. */
    @Column(name = "category_group", nullable = false, length = 5)
    private String categoryGroup;

    /** Short descriptive title for UI display */
    @Column(name = "title", length = 250)
    private String title;

    @Column(name = "text_english", columnDefinition = "TEXT")
    private String textEnglish;

    @Column(name = "text_spanish", columnDefinition = "TEXT")
    private String textSpanish;

    @Column(name = "text_chinese", columnDefinition = "TEXT")
    private String textChinese;

    @Column(name = "text_armenian", columnDefinition = "TEXT")
    private String textArmenian;

    /** True when text contains {VAR} placeholders requiring substitution */
    @Column(name = "has_variables")
    private Boolean hasVariables = false;

    public NoaCategoryMessageEntity() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryGroup() { return categoryGroup; }
    public void setCategoryGroup(String categoryGroup) { this.categoryGroup = categoryGroup; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTextEnglish() { return textEnglish; }
    public void setTextEnglish(String textEnglish) { this.textEnglish = textEnglish; }

    public String getTextSpanish() { return textSpanish; }
    public void setTextSpanish(String textSpanish) { this.textSpanish = textSpanish; }

    public String getTextChinese() { return textChinese; }
    public void setTextChinese(String textChinese) { this.textChinese = textChinese; }

    public String getTextArmenian() { return textArmenian; }
    public void setTextArmenian(String textArmenian) { this.textArmenian = textArmenian; }

    public Boolean getHasVariables() { return hasVariables; }
    public void setHasVariables(Boolean hasVariables) { this.hasVariables = hasVariables; }
}
