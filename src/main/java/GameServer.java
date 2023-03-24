import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameServer extends GameMeta implements IGameServer {

    private static final Logger logger = Logger.getLogger(GameServer.class);

    private GameState gameState;
    private final KeyBasedLock positionLock;
    private IBackupServer backupServer;
    private final Registry registry;
    private final ITracker tracker;

    public GameServer(Game game) {
        super(game);
        this.registry = game.getRegistry();
        this.tracker = game.getTracker();

        List<Position> allPositions = Position.generateAllPositions(this.getGridSize());
        Set<String> keys = allPositions.stream().map(Position::getPositionKey).collect(Collectors.toSet());
        this.positionLock = new KeyBasedLock(keys);
    }

    public void initialize() {
        this.lookupBackupServer();

        try {
            this.initializeGameState();
            this.syncServerInfo();
            this.tryBackupGameData(this.gameState.exportGameData());

            IGameServer stub = (IGameServer) UnicastRemoteObject.exportObject(this, Constants.PRIMARY_SERVER_PORT);
            this.registry.rebind(Constants.PRIMARY_GAME_SERVER_STUB_NAME, stub);
            logger.info("Primary server ready!");
        } catch (Exception e) {
            logger.error("Server exception: " + e);
            e.printStackTrace();
        }
    }

    private void initializeGameState() {
        this.gameState = GameState.newGameState(this.getGridSize(), this.getTreasureSize());
    }

    public void respawn() {
        this.lookupBackupServer();

        try {
            GameStateData data = this.getBackupGameData();
            logger.info("Backup data found: " + (data == null ? "null" : data.toString()));
            this.copyGameState(data);
            this.syncGamePlayers();
            this.syncServerInfo();
            this.tryBackupGameData(this.gameState.exportGameData());

            IGameServer stub = (IGameServer) UnicastRemoteObject.exportObject(this, Constants.PRIMARY_SERVER_PORT);
            this.registry.rebind(Constants.PRIMARY_GAME_SERVER_STUB_NAME, stub);
            logger.info("Primary Server respawned!");
        } catch (Exception e) {
            logger.error("Server exception: " + e);
            e.printStackTrace();
        }
    }

    private void lookupBackupServer() {
        if (this.backupServer != null) {
            return;
        }
        try {
            this.backupServer = (IBackupServer) this.registry.lookup(Constants.BACKUP_GAME_SERVER_STUB_NAME);
            logger.info("Backup server found!");
        } catch (Exception e) {
            logger.warn("Backup server not found!");
        }
    }

    private void copyGameState(GameStateData data) {
        if (data == null) {
            this.initializeGameState();
        } else {
            this.gameState = new GameState(data);
        }
    }

    private void syncServerInfo() {
        try {
            ServerInfo serverInfo = this.tracker.getServerInfo();
            this.gameState.setPrimaryServer(serverInfo.getPrimaryServer());
            this.gameState.setBackupServer(serverInfo.getBackupServer());
        } catch (Exception e) {
            logger.error("Sync server info failed! " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncGamePlayers() {
        try {
            Set<String> currentPlayerIds = new HashSet<>(this.gameState.getAllPlayers());
            Set<String> accuratePlayerIds = this.tracker.getPlayerSet();

            Set<String> commonPlayers = new HashSet<>(currentPlayerIds);
            commonPlayers.retainAll(accuratePlayerIds);

            // to be added players
            accuratePlayerIds.removeAll(commonPlayers);
            // to be removed players
            currentPlayerIds.removeAll(commonPlayers);

            logger.info("To be add players: " + accuratePlayerIds);
            logger.info("To be remove players: " + currentPlayerIds);
            this.gameState.initializeNewPlayers(accuratePlayerIds, this.getGridSize());
            this.gameState.removePlayers(currentPlayerIds);
        } catch (Exception e) {
            logger.warn("Sync player failed!");
        }
    }

    private GameStateData getBackupGameData() {
        try {
            if (this.backupServer != null) {
                return this.backupServer.getGameData();
            }
        } catch (Exception e) {
            logger.warn("Backup server not found when getting backup data!");
        }
        return null;
    }

    private void tryBackupGameData(GameStateData gameStateData) {
        try {
            this.lookupBackupServer();
            if (this.backupServer != null) {
                this.backupServer.updateGameData(gameStateData);
            }
        } catch (Exception e) {
            logger.warn("Backup game data failed! error=" + e.getMessage());
            // will lookup backup server next time
            this.backupServer = null;
        }
    }

    private boolean tryAccessPosition(Position position) {
        if (this.positionLock.tryLock(position.getPositionKey())) {
            if (this.gameState.isPositionOccupied(position)) {
                // if position is occupied, just unlock it and return false
                unlockPosition(position);
                return false;
            } else {
                // position will be unlocked after all processing
                return true;
            }
        } else {
            return false;
        }
    }

    private void unlockPosition(Position position) {
        if (position != null) {
            this.positionLock.unlock(position.getPositionKey());
        }
    }

    @Override
    public GameStateData getGameData() throws RemoteException {
        return this.gameState.exportGameData();
    }

    @Override
    public GameStateData addPlayer(String playerId) {
        Position position = findNewAvailablePosition();
        try {
            this.gameState.addPlayer(playerId, position);
        } finally {
            unlockPosition(position);
        }
        logger.info("Player added successfully, player=" + playerId);
        GameStateData gameData = this.gameState.exportGameData();
        this.tryBackupGameData(gameData);
        return gameData;
    }

    @Override
    public GameStateData movePlayer(String playerId, Direction direction) {
        PlayerInfo player = this.gameState.getPlayerMap().get(playerId);
        if (player == null) {
            logger.warn("player not found, playerId=" + playerId);
            return this.gameState.exportGameData();
        }
        Position newPosition = player.getPosition().movePosition(direction);
        if (!newPosition.isValid(this.getGridSize())) {
            logger.warn("player new position out of bound, playerId=" + playerId +
                    ", new position " + newPosition);
            return this.gameState.exportGameData();
        }

        boolean success = tryMove(newPosition);
        if (!success) {
            logger.info("player move failed, position is snatched");
            return this.gameState.exportGameData();
        }

        boolean treasureFound = tryCollectTreasure(newPosition);
        Position newTreasurePosition = null;
        if (treasureFound) {
            newTreasurePosition = findNewAvailablePosition();
        }
        try {
            this.gameState.movePlayerAndFollowUp(player, newPosition, newTreasurePosition);
        } catch (Exception e) {
            logger.error("error happens when moving player, adding points or adding new treasure, error=" + e.getMessage());
        } finally {
            unlockPosition(newPosition);
            unlockPosition(newTreasurePosition);
        }

        GameStateData gameData = this.gameState.exportGameData();
        this.tryBackupGameData(gameData);
        return gameData;
    }

    @Override
    public void changeBackupServer(String backupServer) throws RemoteException {
        this.gameState.setBackupServer(backupServer);
    }

    private boolean tryMove(Position newPosition) {
        try {
            boolean success = tryAccessPosition(newPosition);
            if (!success) {
                return false;
            }
        } catch (Exception e) {
            logger.warn("player move failed, error=" + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean tryCollectTreasure(Position playerPosition) {
        Set<Position> treasurePositions = this.gameState.getTreasurePositions();
        return treasurePositions.contains(playerPosition);
    }

    private Position findNewAvailablePosition() {
        Position position = this.gameState.getRandomAvailablePosition(this.getGridSize());
        boolean success = tryAccessPosition(position);
        if (success) {
            return position;
        } else {
            return findNewAvailablePosition();
        }
    }

    @Override
    public void removePlayer(String playerId) throws RemoteException {
        this.gameState.removePlayer(playerId);
        this.tryBackupGameData(this.gameState.exportGameData());
    }
}
