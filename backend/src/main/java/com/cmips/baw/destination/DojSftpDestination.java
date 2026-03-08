package com.cmips.baw.destination;

import com.cmips.integration.framework.baw.destination.Destination;
import com.cmips.integration.framework.baw.destination.Sftp;

/**
 * SFTP outbound destination for Department of Justice (DOJ).
 *
 * CMNR932A common number records are uploaded here for provider
 * background check matching.
 *
 * File naming convention: CMNR932A_YYYYMMDD_HHMMSS.DAT
 */
@Destination(
    name = "doj-sftp-outbound",
    description = "Department of Justice - Common Number Records (Outbound)"
)
@Sftp(
    host = "${baw.sftp.doj.host}",
    port = 22,
    remotePath = "${baw.sftp.doj.outbound-path}",
    credentials = "doj-sftp-creds",
    connectionTimeout = 30000,
    createDirectory = true,
    tempSuffix = ".tmp",
    knownHosts = "${baw.sftp.doj.known-hosts:}"
)
public interface DojSftpDestination {
    // Marker interface - configuration is in annotations
}
