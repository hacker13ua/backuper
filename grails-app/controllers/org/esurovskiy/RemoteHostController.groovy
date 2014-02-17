package org.esurovskiy

class RemoteHostController {
    def scaffold = true;
    def backupProcessorService;
//    def index() {}

    def tryToConnect =
    {
        if (params.id instanceof Serializable)
        {
//            RemoteHost remoteHost = RemoteHost.get(params.id);
            try
            {
                backupProcessorService.makeFullHostBackup(params.id)
            }
            catch (RuntimeException rExc)
            {
                log.error(rExc);
            }
        }
    }
}
