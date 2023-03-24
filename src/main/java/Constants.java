public final class Constants {

    private Constants() {
        // this should not happen
    }

    public static final int DEFAULT_RMI_REGISTRY_PORT = 1099;
    public static final int PRIMARY_SERVER_PORT = 5223;
    public static final int BACKUP_SERVER_PORT = 5224;

    public static final String DEFAULT_RMI_REGISTRY_HOST = null;

    public static final String PRIMARY_GAME_SERVER_STUB_NAME = "GameServer";
    public static final String BACKUP_GAME_SERVER_STUB_NAME = "BackupGameServer";

    public static final String TRACKER_STUB_NAME = "Tracker";
}
