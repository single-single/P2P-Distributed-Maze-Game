public class StatusChecker extends Thread {

    private static final Logger logger = Logger.getLogger(StatusChecker.class);

    private final String playerID;
    private ClientType addedType;
    private final ITracker tracker;
    private final Game game;

    public StatusChecker(ClientType addedType, Game game) {
        this.playerID = game.getPlayerID();
        this.addedType = addedType;
        this.tracker = game.getTracker();
        this.game = game;
    }

    @Override
    public void run() {
        ClientType newType = null;

        while(true) {
            try {
                newType = tracker.ping(playerID);
            } catch (Exception e) {
                logger.error("Failed to ping tracker, player=" + playerID);
            }
            try {
                if (newType != null) {
                    if (newType != addedType) {
                        this.addedType = newType;
                        if (newType == ClientType.PRIMARY_SERVER) {
                            GameServer gameServer = new GameServer(this.game);
                            game.setPrimaryServer(gameServer);
                            game.getPrimaryServer().respawn();
                        } else if (newType == ClientType.BACKUP_SERVER) {
                            BackupServer backupServer = new BackupServer(this.game);
                            game.setBackupServer(backupServer);
                            game.getBackupServer().initialize();
                        }
                    }
                }

                Thread.sleep(500);
            } catch (Exception e) {
                logger.error("Error happens when handling ping result, player=" + playerID);
            }
        }

    }
}
