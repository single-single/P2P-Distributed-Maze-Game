import java.io.Serializable;

public class GameMeta implements Serializable {

    private static final long serialVersionUID = 3193261559498876273L;

    private int gridSize;
    private int treasureSize;

    public GameMeta() {}

    public GameMeta(GameMeta that) {
        this.gridSize = that.getGridSize();
        this.treasureSize = that.getTreasureSize();
    }

    public GameMeta(int gridSize, int treasureSize) {
        this.gridSize = gridSize;
        this.treasureSize = treasureSize;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getTreasureSize() {
        return treasureSize;
    }

    public void setGameMeta(GameMeta meta) {
        this.gridSize = meta.getGridSize();
        this.treasureSize = meta.getTreasureSize();
    }
}
