package com.cmips.baw.destination;

import com.cmips.integration.framework.baw.destination.Destination;
import com.cmips.integration.framework.baw.destination.Sftp;

/**
 * SFTP source configuration for Electronic Visit Verification (EVV) vendor.
 *
 * DSD Section 24 — EVV vendor sends daily check-in/check-out data to CMIPS.
 * Files contain provider visits with GPS coordinates, timestamps, and service types.
 *
 * Inbound files are downloaded from this source for processing and comparison
 * against existing timesheet data.
 */
@Destination(
    name = "evv-sftp",
    description = "EVV Vendor - Daily Visit Verification Files (Inbound)"
)
@Sftp(
    host = "${baw.sftp.evv.host}",
    port = 22,
    remotePath = "${baw.sftp.evv.inbound-path}",
    credentials = "evv-sftp-creds",
    connectionTimeout = 30000,
    createDirectory = false,
    tempSuffix = "",
    knownHosts = "${baw.sftp.evv.known-hosts:}"
)
public interface EvvSftpSource {
    // Marker interface - configuration is in annotations
}
