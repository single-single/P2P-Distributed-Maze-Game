import java.io.Serializable;

public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 5923263686128663841L;

    private String primaryServer;
    private String backupServer;

    public ServerInfo() {}

    public ServerInfo(ServerInfo that) {
        this.primaryServer = that.getPrimaryServer();
        this.backupServer = that.getBackupServer();
    }

    public ServerInfo(String primaryServer, String backupServer) {
        this.primaryServer = primaryServer;
        this.backupServer = backupServer;
    }

    public String getPrimaryServer() {
        return primaryServer;
    }

    public void setPrimaryServer(String primaryServer) {
        this.primaryServer = primaryServer;
    }

    public String getBackupServer() {
        return backupServer;
    }

    public void setBackupServer(String backupServer) {
        this.backupServer = backupServer;
    }
}
