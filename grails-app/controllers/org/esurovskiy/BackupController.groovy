package org.esurovskiy

class BackupController {

    def backupProcessorService
    def successFinishEvent()
    {
        println "success execute finish event. filename=${params.fileName} and hostId=${params.hostId}"
        backupProcessorService.downloadBackupToServer(params.hostId, params.fileName);
    }

    def failFinishEvent()
    {
        println "fail execute finish event. filename=${params.fileName} and hostId=${params.hostId}"
    }
}
