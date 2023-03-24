import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameGui extends GameMeta {

    private static final Logger logger = Logger.getLogger(GameGui.class);

    private static final String TABLE_STYLE = "<style> td { border:1px solid black; } </style>";
    private static final String NEW_LINE = "<br />";
    private static final String HTML_TAG = "<html>";
    private static final String TABLE_TAG = "<table>";
    private static final String TR_TAG = "<tr>";
    private static final String TD_TAG = "<td>";
    private static final String HTML_TAG_CLOSE = "</html>";
    private static final String TABLE_TAG_CLOSE = "</table>";
    private static final String TR_TAG_CLOSE = "</tr>";
    private static final String TD_TAG_CLOSE = "</td>";

    private final JLabel leaderBoard = new JLabel("Loading LeaderBoard...");
    private final JLabel maze = new JLabel("Loading Maze...");

    public GameGui(GameMeta meta) {
        super(meta);
    }

    public void initialize(String playerId) {
        try {
            JFrame frame = new JFrame(playerId);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 500);

            EmptyBorder border = new EmptyBorder(10, 10, 10, 10);

            JPanel panel1 = new JPanel();
            panel1.setBorder(border);
            panel1.add(this.leaderBoard);

            JPanel panel2 = new JPanel();
            panel2.setBorder(border);
            panel2.add(this.maze);

            frame.getContentPane().add(BorderLayout.WEST, panel1);
            frame.getContentPane().add(BorderLayout.EAST, panel2);
            frame.setVisible(true);
        } catch (Exception e) {
            logger.error("Error happens when initializing UI, playerId=" + playerId);
            e.printStackTrace();
        }
    }

    public void render(GameStateData data) {
        // update leaderboard
        List<PlayerInfo> players = data.getPlayerMap().values().stream().sorted().collect(Collectors.toList());
        StringBuilder sb1 = new StringBuilder(HTML_TAG);
        for (PlayerInfo player : players) {
            sb1.append(player.getId()).append(": ").append(player.getPoints()).append("pts").append(NEW_LINE);
        }
        String backupServer = data.getBackupServer() != null ? data.getBackupServer() : "";
        sb1.append(NEW_LINE).append("Primary: ").append(data.getPrimaryServer());
        sb1.append(NEW_LINE).append("Backup: ").append(backupServer);
        sb1.append(HTML_TAG_CLOSE);
        this.leaderBoard.setText(sb1.toString());

        // update maze
        Set<Position> treasurePositions = data.getTreasurePositions();
        Map<Position, PlayerInfo> playerMap = players.stream().collect(
                Collectors.toMap(PlayerInfo::getPosition, Function.identity())
        );
        StringBuilder sb2 = new StringBuilder(HTML_TAG).append(TABLE_STYLE).append(TABLE_TAG);
        for (int y = 0; y < this.getGridSize(); y++) {
            sb2.append(TR_TAG);
            for (int x = 0; x < this.getGridSize(); x++) {
                sb2.append(TD_TAG);
                Position pos = new Position(x, y);
                if (treasurePositions.contains(pos)) {
                    sb2.append("**&nbsp&nbsp");
                } else if (playerMap.containsKey(pos)) {
                    sb2.append(playerMap.get(pos).getId());
                } else {
                    sb2.append("&nbsp&nbsp&nbsp&nbsp&nbsp");
                }
                sb2.append(TD_TAG_CLOSE);
            }
            sb2.append(TR_TAG_CLOSE);
        }
        sb2.append(TABLE_TAG_CLOSE);
        sb2.append(HTML_TAG_CLOSE);
        this.maze.setText(sb2.toString());
    }
}
