import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameStateData extends ServerInfo implements Serializable {

    private static final long serialVersionUID = 1252902354451108217L;

    private final Map<String, PlayerInfo> playerMap;
    private final Set<Position> treasurePositions;

    public GameStateData(GameState gameState) {
        super(gameState);
        this.playerMap = new HashMap<>(gameState.getPlayerMap());
        this.treasurePositions = new HashSet<>(gameState.getTreasurePositions());
    }

    public GameStateData(GameStateData gameStateData) {
        super(gameStateData);
        this.playerMap = new HashMap<>(gameStateData.getPlayerMap());
        this.treasurePositions = new HashSet<>(gameStateData.getTreasurePositions());
    }

    public Map<String, PlayerInfo> getPlayerMap() {
        return playerMap;
    }

    public Set<Position> getTreasurePositions() {
        return treasurePositions;
    }

    @Override
    public String toString() {
        return "GameStateData{" +
                "playerMap=" + playerMap +
                ", treasurePositions=" + treasurePositions +
                '}';
    }
}
