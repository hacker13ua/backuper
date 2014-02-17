package org.esurovskiy

class PostgresStorage //extends AbstractStorage
{
    static belongsTo = [remoteHost: RemoteHost]
    Integer psqlPort;
    String postgresUser;
    String databaseName;
    static constraints = {
        psqlPort(size:1..65535);
        postgresUser(nullable: true);
        databaseName(blank: false);
    }



//    @Override
    String getBackupCommand(final String fileName)
    {
        return "pg_dump --host 127.0.0.1 --port ${psqlPort} --username ${postgresUser} --format custom --blobs --encoding UTF8 --verbose --file ${fileName} ${databaseName}"
    }
}
