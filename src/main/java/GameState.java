import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameState extends ServerInfo {

    private final ConcurrentHashMap<String, PlayerInfo> playerMap = new ConcurrentHashMap<>();
    private final Set<Position> treasurePositions;

    private GameState(List<Position> treasurePositions) {
        super();
        this.treasurePositions = ConcurrentHashMap.newKeySet(treasurePositions.size());
        this.treasurePositions.addAll(treasurePositions);
    }

    public GameState(GameStateData data) {
        super(data);
        this.playerMap.putAll(data.getPlayerMap());
        this.treasurePositions = ConcurrentHashMap.newKeySet(data.getTreasurePositions().size());
        this.treasurePositions.addAll(data.getTreasurePositions());
    }

    public static GameState newGameState(int gridSize, int treasureSize) {
        List<Position> treasurePositions = Position.randomPositions(gridSize, treasureSize);
        return new GameState(treasurePositions);
    }

    public ConcurrentHashMap<String, PlayerInfo> getPlayerMap() {
        return this.playerMap;
    }

    public Set<String> getAllPlayers() {
        return this.playerMap.keySet();
    }

    public boolean isPositionOccupied(Position position) {
        for (PlayerInfo player : this.playerMap.values()) {
            if (player.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    public Set<Position> getTreasurePositions() {
        return this.treasurePositions;
    }

    public Position getRandomAvailablePosition(int gridSize) {
         List<Position> allPositions = Position.generateAllPositions(gridSize);
         List<Position> playerPositions = this.playerMap.values().stream().map(PlayerInfo::getPosition)
                 .collect(Collectors.toList());
         allPositions.removeAll(playerPositions);
         allPositions.removeAll(this.treasurePositions);
         // randomly choose one position
         int index = ThreadLocalRandom.current().nextInt(0, allPositions.size());
         return allPositions.get(index);
    }

    public List<Position> getRandomAvailablePositions(int gridSize, int numPositions) {
        List<Position> allPositions = Position.generateAllPositions(gridSize);
        List<Position> playerPositions = this.playerMap.values().stream().map(PlayerInfo::getPosition)
                .collect(Collectors.toList());
        allPositions.removeAll(playerPositions);
        allPositions.removeAll(this.treasurePositions);
        Collections.shuffle(allPositions);
        // randomly choose n position
        return allPositions.subList(0, numPositions);
    }

    public synchronized void addPlayer(String playerId, Position initialPosition) {
        PlayerInfo info = new PlayerInfo(playerId, initialPosition);
        this.playerMap.put(playerId, info);
    }

    public synchronized void initializeNewPlayers(Set<String> players, int gridSize) {
        if (players.isEmpty()) {
            return;
        }
        List<Position> playerPositions = this.getRandomAvailablePositions(gridSize, players.size());
        int i = 0;
        for (String playerId: players) {
            Position pos = playerPositions.get(i);
            this.playerMap.put(playerId, new PlayerInfo(playerId, pos));
            i++;
        }
    }

    public synchronized void movePlayerAndFollowUp(PlayerInfo player, Position newPlayerPosition,
                                                   Position newTreasurePosition) {
        player.setPosition(newPlayerPosition);
        if (newTreasurePosition != null) {
            player.incrementPoint();
            this.treasurePositions.remove(newPlayerPosition);
            this.treasurePositions.add(newTreasurePosition);
        }
    }

    public synchronized void removePlayer(String playerId) {
        this.playerMap.remove(playerId);
        // TODO: unlock all user lock? check again, all lock should be release within same method same thread
    }

    public synchronized void removePlayers(Set<String> players) {
        this.playerMap.keySet().removeAll(players);
    }

    public synchronized GameStateData exportGameData() {
        return new GameStateData(this);
    }
}
