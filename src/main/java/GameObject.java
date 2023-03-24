public enum GameObject {

    FLOOR('F'),
    TREASURE('T'),
    PLAYER('P');

    private final char SYMBOL;

    GameObject(final char symbol) {
        SYMBOL = symbol;
    }

    public static GameObject FromChar(char c) {
        for (GameObject t : GameObject.values()) {
            if (Character.toUpperCase(c) == t.SYMBOL) {
                return t;
            }
        }

        return FLOOR;
    }

    public char GetCharSymbol() {
        return SYMBOL;
    }
}
