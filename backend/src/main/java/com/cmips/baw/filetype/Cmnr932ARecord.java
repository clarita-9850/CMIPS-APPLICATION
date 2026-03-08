package com.cmips.baw.filetype;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CMNR932A — Common Number Record for DOJ (fixed-width, 44 bytes).
 *
 * Outbound to DOJ (Department of Justice) for provider background check matching.
 * One record per provider requiring DOJ cross-reference.
 *
 * Layout:
 * - Pos 1-10:  File ID ("CMNR932A  ", 10 chars)
 * - Pos 11-14: Record Type ("DTL " or "HDR " or "TRL ", 4 chars)
 * - Pos 15-24: Provider ID (10 digits, zero-padded right)
 * - Pos 25-34: Assignment Status (10 chars: ACTIVE, INACTIVE, PENDING)
 * - Pos 35-44: Filler (10 chars)
 *
 * Total record length: 44 characters
 */
@FileType(
    name = "common-number-doj-932a",
    description = "DOJ Common Number Record (CMNR932A)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cmnr932ARecord {

    @FileColumn(
        order = 1,
        name = "FILE_ID",
        length = 10,
        nullable = false
    )
    @Builder.Default
    private String fileId = "CMNR932A";

    @FileColumn(
        order = 2,
        name = "RECORD_TYPE",
        length = 4,
        nullable = false
    )
    @Builder.Default
    private String recordType = "DTL";

    @FileId
    @Validate(notNull = true, message = "Provider ID is required")
    @FileColumn(
        order = 3,
        name = "PROVIDER_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Long providerId;

    @Validate(notBlank = true, allowedValues = {"ACTIVE", "INACTIVE", "PENDING"}, message = "Status must be ACTIVE, INACTIVE, or PENDING")
    @FileColumn(
        order = 4,
        name = "ASSIGNMENT_STATUS",
        length = 10,
        nullable = false
    )
    @Builder.Default
    private String assignmentStatus = "ACTIVE";

    @FileColumn(
        order = 5,
        name = "FILLER",
        length = 10,
        nullable = true
    )
    private String filler;

    public static Cmnr932ARecord fromProvider(Long providerId, String status) {
        return Cmnr932ARecord.builder()
                .providerId(providerId)
                .assignmentStatus(status != null ? status : "ACTIVE")
                .build();
    }
}
