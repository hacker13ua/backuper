package org.esurovskiy

class LocalFileSystemStorage //extends AbstractStorage
{

    static belongsTo = [remoteHost: RemoteHost]
    String pathToBackup;
    static constraints = {
        pathToBackup(blank: false)
    }

//    @Override
    String getBackupCommand(final String fileName)
    {
        return null;
    }
}
