package com.cmips.baw.destination;

import com.cmips.integration.framework.baw.destination.Destination;
import com.cmips.integration.framework.baw.destination.Sftp;

/**
 * SFTP outbound destination for Employment Development Department (EDD).
 *
 * PRDS943B quarterly payroll detail files are uploaded here for
 * unemployment insurance wage reporting.
 *
 * File naming convention: PRDS943B_QNYYYY_YYYYMMDD_HHMMSS.DAT
 */
@Destination(
    name = "edd-sftp-outbound",
    description = "Employment Development Department - Payroll Detail Files (Outbound)"
)
@Sftp(
    host = "${baw.sftp.edd.host}",
    port = 22,
    remotePath = "${baw.sftp.edd.outbound-path}",
    credentials = "edd-sftp-creds",
    connectionTimeout = 30000,
    createDirectory = true,
    tempSuffix = ".tmp",
    knownHosts = "${baw.sftp.edd.known-hosts:}"
)
public interface EddSftpDestination {
    // Marker interface - configuration is in annotations
}
