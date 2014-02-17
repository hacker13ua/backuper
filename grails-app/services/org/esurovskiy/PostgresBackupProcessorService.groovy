package org.esurovskiy

import grails.transaction.Transactional

@Transactional
class PostgresBackupProcessorService extends BackupProcessorService
{

    def serviceMethod() {

    }

    @Override
    void processNewBackupOnRemoteHost(final RemoteHost remoteHost)
    {

    }
}
