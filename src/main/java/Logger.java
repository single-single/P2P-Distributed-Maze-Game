public class Logger {

    private final String className;

    private Logger(String className) {
        this.className = className;
    }

    public static Logger getLogger(Class clazz) {
        return new Logger(clazz == null ? "Null" : clazz.getName());
    }

    public void info(String msg) {
        System.out.println("[Info]" + "[" + this.className + "] " + msg);
    }

    public void warn(String msg) {
        System.out.println("[Warning]" + "[" + this.className + "] " + msg);
    }

    public void error(String msg) {
        System.out.println("[Error]" + "[" + this.className + "] " + msg);
    }

    public static void logGameData(Logger logger, GameStateData gameData) {
        logger.info("Game data shown below:");
        for (PlayerInfo player : gameData.getPlayerMap().values()) {
            logger.info(player.getId() + " Points: " + player.getPoints() + " Pos: " + player.getPosition().toString());
        }
        int index = 1;
        for (Position treasurePos : gameData.getTreasurePositions()) {
            logger.info("Treasure" + index + ": " + treasurePos.toString());
            index++;
        }
    }
}
