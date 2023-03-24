import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Tracker extends GameMeta implements ITracker{
	private static final Logger logger = Logger.getLogger(Tracker.class);
	private final ConcurrentHashMap<String, Long> alivePlayers;
	private String primaryServer;
	private String backupServer;
	private IGameServer gameServer;

	public Tracker(int treasureSize, int gridSize) {
		super(gridSize, treasureSize);
		this.alivePlayers = new ConcurrentHashMap<>();
	}

	public static void main(String[] args) {
		if(args.length != 3){
			logger.error("Wrong parameters");
			System.exit(0);
			return;
		}

		int port = Integer.parseInt(args[0]);
		int gridSize = Integer.parseInt(args[1]);
		int treasureSize = Integer.parseInt(args[2]);
		Registry registry;
		ITracker stub;
		if (port == Constants.DEFAULT_RMI_REGISTRY_PORT) {
			logger.error("port 1099 is used by rmi registry");
			System.exit(0);
			return;
		}

		Tracker tracker = new Tracker(treasureSize, gridSize);
		try {
			stub = (ITracker) UnicastRemoteObject.exportObject(tracker, port);
			registry = LocateRegistry.getRegistry();
			registry.rebind(Constants.TRACKER_STUB_NAME, stub);
			logger.info("Tracker ready");
		} catch (Exception e) {
			logger.error("Tracker exception: " + e);
			e.printStackTrace();
		}

		PingChecker pingChecker = new PingChecker(tracker, tracker.alivePlayers);
		pingChecker.start();

//		ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
//				new BasicThreadFactory.
//						Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());

//		ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
//		pool.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//				ConcurrentHashMap<String, Long> map = tracker.alivePlayers;
//				long cur = System.currentTimeMillis();
//				for (Map.Entry<String, Long> entry : map.entrySet()) {
//					if (cur - entry.getValue() > 750) {
//						String playerId = entry.getKey();
//						try {
//							System.out.println("Player " + playerId + " crashed");
//							tracker.quit(playerId);
//						} catch (RemoteException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}, 0, 100, TimeUnit.MILLISECONDS);

	}

	@Override
	public synchronized ClientType addPlayer(String playerId) throws RemoteException {
		logger.info("Add player " + playerId);
		alivePlayers.put(playerId, System.currentTimeMillis());
		if (primaryServer == null) {
			primaryServer = playerId;
			return ClientType.PRIMARY_SERVER;
		} else if (backupServer == null) {
			backupServer = playerId;
			return ClientType.BACKUP_SERVER;
		}
		return ClientType.PLAYER;
	}

	@Override
	public ClientType ping(String playerId) throws RemoteException {
		if (primaryServer != null && gameServer == null && primaryServer.equals(playerId)) {
			try {
				gameServer = (IGameServer) LocateRegistry.getRegistry(Constants.DEFAULT_RMI_REGISTRY_PORT).lookup(Constants.PRIMARY_GAME_SERVER_STUB_NAME);
				logger.info("Tracker get Server stub");
			} catch (NotBoundException e) {
				logger.error("Tracker fail to get Server stub");
			}
		}

		alivePlayers.replace(playerId, System.currentTimeMillis());
		if (primaryServer != null && primaryServer.equals(playerId)) {
			return ClientType.PRIMARY_SERVER;
		} else if (backupServer != null && backupServer.equals(playerId)) {
			return ClientType.BACKUP_SERVER;
		} else {
			return ClientType.PLAYER;
		}
	}

	@Override
	public void quit(String playerId) throws RemoteException {
		logger.info(playerId + "quit");
		alivePlayers.remove(playerId);
		if (primaryServer != null && primaryServer.equals(playerId)) {
			primaryServer = selectNewServer("primary server");
			gameServer = null;
			Registry registry = LocateRegistry.getRegistry();
			try {
				registry.unbind(Constants.PRIMARY_GAME_SERVER_STUB_NAME);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		} else if (backupServer != null && backupServer.equals(playerId)) {
			backupServer = selectNewServer("backup server");
			tryRemovePlayer(playerId);
		} else {
			tryRemovePlayer(playerId);
		}
	}

	private void tryRemovePlayer(String playerId) throws RemoteException {
		if (this.gameServer != null) {
			this.gameServer.removePlayer(playerId);
		}
	}

	@Override
	public GameMeta getGameMeta() throws RemoteException {
		return new GameMeta(this.getGridSize(), this.getTreasureSize());
	}

	@Override
	public Set<String> getPlayerSet() throws RemoteException {
		return alivePlayers.keySet();
	}

	@Override
	public ServerInfo getServerInfo() throws RemoteException {
		return new ServerInfo(this.primaryServer, this.backupServer);
	}


	private String selectNewServer(String server) {
		for (String playerId : alivePlayers.keySet()) {
			if (!playerId.equals(primaryServer) && !playerId.equals(backupServer)) {
				logger.info(playerId + " is selected as the new " + server);
				return playerId;
			}
		}
		return null;
	}
}
