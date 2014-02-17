class BootStrap
{
    def init = {
        servletContext ->
            BackupJobScheduler.instance.startup()
    }
    def destroy = {
        BackupJobScheduler.instance.shutdown()
    }
}
