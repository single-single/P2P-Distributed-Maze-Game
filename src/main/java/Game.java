import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Game extends GameMeta {

    private static final Logger logger = Logger.getLogger(Game.class);

    private final String playerID;

    private final String trackerHost;

    private GameStateData gameStateData;

    private GameServer primaryServer;

    private BackupServer backupServer;

    private IGameServer gameServer;

    private ITracker tracker;

    private Registry registry;

    private StatusChecker statusChecker;

    private GameGui gui;

    public Game(String playerID, String trackerHost) {
        super();
        this.playerID = playerID;
        this.trackerHost = trackerHost;
    }

    public String getPlayerID() {
        return playerID;
    }

    public ITracker getTracker() {
        return tracker;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setTracker(ITracker tracker) {
        this.tracker = tracker;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getTrackerHost() {
        return trackerHost;
    }

    public void setPrimaryServer(GameServer primaryServer) {
        this.primaryServer = primaryServer;
    }

    public GameServer getPrimaryServer() {
        return primaryServer;
    }

    public BackupServer getBackupServer() {
        return backupServer;
    }

    public void setBackupServer(BackupServer backupServer) {
        this.backupServer = backupServer;
    }

    public GameStateData getGameStateData() {
        return gameStateData;
    }

    public void setGameStateData(GameStateData gameStateData) {
        this.gameStateData = gameStateData;
    }

    public GameGui getGui() {
        return gui;
    }

    public void setGui(GameGui gui) {
        this.gui = gui;
    }

    public void registerPrimaryServer() {
        GameServer gameServer = new GameServer(this);
        gameServer.initialize();
        this.primaryServer = gameServer;
    }

    public void registerBackupServer() {
        BackupServer backupServer = new BackupServer(this);
        backupServer.initialize();
        this.backupServer = backupServer;
    }

    private ClientType addPlayerToTracker() {
        ClientType type = null;

        try {
            type = this.tracker.addPlayer(this.playerID);
        } catch (Exception e) {
            logger.error("Failed to add player to tracker");
            System.exit(0);
        }

        logger.info("Client type is " + type.name());
        statusChecker = new StatusChecker(type, this);
        statusChecker.start();

        if (type == ClientType.PRIMARY_SERVER) {
            this.registerPrimaryServer();
        } else if (type == ClientType.BACKUP_SERVER) {
            this.registerBackupServer();
        }

        return type;
    }

    private void addPlayerToServer() throws RemoteException {
        this.gameStateData = this.gameServer.addPlayer(this.playerID);
    }

    public boolean operate(String operation) throws Exception {
        try {
            // Return value for handling fault
            switch (operation) {
                case "0":
                    logger.info("Refresh");
                    this.gameStateData = gameServer.getGameData();
                    return true;
                case "1":
                case "2":
                case "3":
                case "4":
                    int directionNumber = Integer.parseInt(operation);
                    Direction direction = Direction.getDirectionByNumber(directionNumber);
                    logger.info("Player move to " + direction.name());
                    this.gameStateData = gameServer.movePlayer(playerID, direction);
                    return true;
                case "9":
                    logger.info("Quit");
                    try {
                        tracker.quit(playerID);
                    } catch (RemoteException e) {
                        logger.error("Failed to quit player" + playerID);
                        System.exit(0);
                    }
                    return true;

                default:
                    logger.warn("Invalid operation");
                    return false;
            }
        } catch (Exception e) {
            logger.warn("Game operation failed, re-lookup game server.");
            this.lookupGameServer();
            return operate(operation);
        }
    }

    public void quit(Tracker tracker) {
        try {
            tracker.quit(playerID);
        } catch (RemoteException e) {
            logger.warn("Failed to quit player" + playerID);
            System.exit(0);
        }

        statusChecker.interrupt();
    }

    public static void main(String[] args) {
        String playerID;
        String trackerHost;

        if (args.length != 3) {
            logger.error("Missing parameters");
            System.exit(0);
        }

        playerID = args[2];
        trackerHost = args[0];

        final Game game = new Game(playerID, trackerHost);
        try {
            Registry trackerRegistry = LocateRegistry.getRegistry(trackerHost, Constants.DEFAULT_RMI_REGISTRY_PORT);
            ITracker tracker = (ITracker) trackerRegistry.lookup(Constants.TRACKER_STUB_NAME);
            game.setRegistry(trackerRegistry);
            game.setTracker(tracker);
        } catch (RemoteException | NotBoundException e) {
            logger.error("Failed to locate Tracker");
            System.exit(0);
        }

        game.initialize();

        System.out.print("Please input your operation:");
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            try {
                String operation = scan.next().trim();
                boolean success = game.operate(operation);
                if (success) {
                    Logger.logGameData(logger, game.getGameStateData());
                    game.getGui().render(game.getGameStateData());
                } else {
                    logger.info("operation failed, game data will not be printed!");
                }
            } catch (Exception e) {
                logger.warn("Failed to execute game operation.");
                e.printStackTrace();
            } finally {
                System.out.print("Please input your operation:");
            }
        }
        scan.close();
    }

    private void initialize() {
        try {
            GameMeta meta = this.tracker.getGameMeta();
            this.setGameMeta(meta);

            this.addPlayerToTracker();

            this.lookupGameServer();

            this.tryAddPlayerToServer();

            GameGui gui = new GameGui(this);
            gui.initialize(this.getPlayerID());
            gui.render(this.getGameStateData());
            this.setGui(gui);

            Logger.logGameData(logger, this.gameStateData);
        } catch (Exception e) {
            logger.error("Game initialization failed!");
            e.printStackTrace();
        }
    }

    private void tryAddPlayerToServer() {
        while (true) {
            try {
                this.addPlayerToServer();
                logger.info("Player successfully added to server! player=" + this.playerID);
                break;
            } catch (Exception e) {
                logger.warn("Add player to server failed, retrying...");
                try {
                    lookupGameServer();
                } catch (Exception exception) {
                    logger.warn("Looking up server interrupted! player=" + this.playerID);
                    exception.printStackTrace();
                }
            }
        }
    }

    private void lookupGameServer() throws InterruptedException {
        while (true) {
            try {
                this.gameServer = (IGameServer) this.registry.lookup(Constants.PRIMARY_GAME_SERVER_STUB_NAME);
                logger.info("Game server found!");
                break;
            } catch (Exception e) {
                logger.info("Retry finding server in 0.1 seconds...");
                Thread.sleep(100);
            }
        }
    }


}
