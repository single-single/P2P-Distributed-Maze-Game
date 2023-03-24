import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PingChecker extends Thread {

	private static final Logger logger = Logger.getLogger(PingChecker.class);

	private final ITracker tracker;
	private final ConcurrentHashMap<String, Long> map;

	public PingChecker(ITracker tracker, ConcurrentHashMap<String, Long> map) {
		this.map = map;
		this.tracker = tracker;
	}

	@Override
	public void run() {
		try {
			while (true) {
				long cur = System.currentTimeMillis();
				for (Map.Entry<String, Long> entry : map.entrySet()) {
					long elapsedTime = cur - entry.getValue();
					if (elapsedTime > 1300) {
						logger.info("Time exceeds, time=" + elapsedTime);
						String playerId = entry.getKey();
						try {
							logger.info("Player " + playerId + " crashed");
							this.tracker.quit(playerId);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			logger.warn("Client exception: " + e.toString());
			e.printStackTrace();
		}

	}

}
