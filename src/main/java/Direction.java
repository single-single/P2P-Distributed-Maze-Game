public enum Direction {
    WEST(1), SOUTH(2), EAST(3), NORTH(4);

    private final int number;

    Direction(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static Direction getDirectionByNumber(int number) {
        for (Direction d : Direction.values()) {
            if (d.getNumber() == number) {
                return d;
            }
        }
        throw new IllegalArgumentException("Number is not a valid direction.");
    }
}
