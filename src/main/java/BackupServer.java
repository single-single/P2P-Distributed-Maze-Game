import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BackupServer implements IBackupServer {

    private static final Logger logger = Logger.getLogger(BackupServer.class);

    private final String playerId;
    private final Registry registry;
    private IGameServer primaryServer;
    private volatile GameStateData gameStateData;

    public BackupServer(Game game) {
        this.playerId = game.getPlayerID();
        this.registry = game.getRegistry();
    }

    @Override
    public GameStateData getGameData() throws RemoteException {
        return new GameStateData(this.gameStateData);
    }

    @Override
    public void updateGameData(GameStateData gameStateData) throws RemoteException {
        this.gameStateData = new GameStateData(gameStateData);
    }

    // this method will be used for both initializing or respawning a backup server
    public void initialize() {
        tryLookupPrimaryServer();
        tryChangeBackupServer();
        trySyncGameData();

        try {
            IBackupServer stub = (IBackupServer) UnicastRemoteObject.exportObject(this, Constants.BACKUP_SERVER_PORT);
            this.registry.rebind(Constants.BACKUP_GAME_SERVER_STUB_NAME, stub);
            logger.info("Back server ready!");
        } catch (Exception e) {
            logger.error("Back server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void tryLookupPrimaryServer() {
        try {
            this.primaryServer = (IGameServer) this.registry.lookup(Constants.PRIMARY_GAME_SERVER_STUB_NAME);
        } catch (Exception e) {
            logger.warn("Looking up primary server failed! error=" + e.getMessage());
        }
    }

    private void trySyncGameData() {
        try {
            if (this.primaryServer != null) {
                this.gameStateData = this.primaryServer.getGameData();
            }
        } catch (Exception e) {
            logger.warn("Sync game data failed! error=" + e.getMessage());
        }
    }

    private void tryChangeBackupServer() {
        try {
            if (this.primaryServer != null) {
                this.primaryServer.changeBackupServer(this.playerId);
            }
        } catch (Exception e) {
            logger.warn("Change backup server failed! error=" + e.getMessage());
        }
    }
}
