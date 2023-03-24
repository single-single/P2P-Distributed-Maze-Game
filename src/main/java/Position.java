import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Position implements Serializable {

    private static final long serialVersionUID = 768016840645708589L;

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position that) {
        this.x = that.getX();
        this.y = that.getY();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void moveX(int x) {
        this.x = this.x + x;
    }

    public void moveY(int y) {
        this.y = this.y + y;
    }

    public String getPositionKey() {
        return this.x + "-" + this.y;
    }

    public Position movePosition(Direction direction) {
        Position newPosition = new Position(this);
        switch (direction) {
            case WEST:
                newPosition.moveX(-1);
                break;
            case SOUTH:
                newPosition.moveY(1);
                break;
            case EAST:
                newPosition.moveX(1);
                break;
            case NORTH:
                newPosition.moveY(-1);
        }
        return newPosition;
    }

    public boolean isValid(int gridSize) {
        return this.x < gridSize && this.y < gridSize && this.x >= 0 && this.y >= 0;
    }

    public static List<Position> randomPositions(int gridSize, int length) {
        List<Integer> xrandomNumbers = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        Collections.shuffle(xrandomNumbers);
        List<Integer> xPositions = xrandomNumbers.subList(0, length);
        List<Integer> yrandomNumbers = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        Collections.shuffle(yrandomNumbers);
        List<Integer> yPositions = yrandomNumbers.subList(0, length);

        List<Position> positions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            positions.add(new Position(xPositions.get(i), yPositions.get(i)));
        }
        return positions;
    }

    public static List<Position> generateAllPositions(int gridSize) {
        List<Position> positions = new ArrayList<>(gridSize * gridSize);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                positions.add(new Position(i, j));
            }
        }
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
