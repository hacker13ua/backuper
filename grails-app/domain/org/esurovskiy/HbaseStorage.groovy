package org.esurovskiy

class HbaseStorage// extends AbstractStorage
{
    static belongsTo = [remoteHost: RemoteHost]
    String tableName;
    static constraints = {
        tableName(blank: false);
    }

//    @Override
    String getBackupCommand(final String fileName)
    {
        return null
    }
}
