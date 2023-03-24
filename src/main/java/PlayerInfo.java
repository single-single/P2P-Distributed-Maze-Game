import java.io.Serializable;

public class PlayerInfo implements Serializable, Comparable<PlayerInfo> {

    private static final long serialVersionUID = -1017570753987964370L;

    private String id;
    private int points = 0;
    private Position position;

    public PlayerInfo(String id, Position position) {
        this.id = id;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void incrementPoint() {
        this.points++;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id='" + id + '\'' +
                ", points=" + points +
                ", position=" + position +
                '}';
    }

    @Override
    public int compareTo(PlayerInfo o) {
        if (this.getPoints() > o.getPoints()) {
            return -1;
        } else if (this.getPoints() < o.getPoints()) {
            return 1;
        } else {
            return this.getId().compareTo(o.getId());
        }
    }
}
