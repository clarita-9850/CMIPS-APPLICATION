package com.cmips.baw.destination;

import com.cmips.integration.framework.baw.destination.Destination;
import com.cmips.integration.framework.baw.destination.Sftp;

/**
 * SFTP source configuration for TPF (Timesheet Processing Facility).
 *
 * TPF sends daily batch files (PRNR998A/C/D) containing scanned paper timesheet data
 * after OCR/ICR processing, verification, and data completion.
 *
 * Inbound files are downloaded from this source for parsing and ingestion.
 *
 * File naming convention: PRNR998_YYYYMMDD_NNN.DAT
 *   where NNN = batch sequence number within the day.
 */
@Destination(
    name = "tpf-sftp",
    description = "Timesheet Processing Facility - Paper Timesheet Batch Files (Inbound)"
)
@Sftp(
    host = "${baw.sftp.tpf.host}",
    port = 22,
    remotePath = "${baw.sftp.tpf.inbound-path}",
    credentials = "tpf-sftp-creds",
    connectionTimeout = 30000,
    createDirectory = false,
    tempSuffix = "",
    knownHosts = "${baw.sftp.tpf.known-hosts:}"
)
public interface TpfSftpSource {
    // Marker interface - configuration is in annotations
}
