import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ITracker extends Remote {
	/**
	 *
	 * @return PRIMARY_SERVER- selected as primary server
	 * 		BACKUP_SERVER - selected as backup server
	 * 		NORMAL_PLAYER - normal player
	 */
	ClientType addPlayer(String playerId) throws RemoteException;

	/**
	 * Players call this method to notify alive
	 * @return PRIMARY_SERVER - selected as primary server
	 * 		    BACKUP_SERVER - selected as backup server
	 * 		    NORMAL_PLAYER - do nothing
	 */
	ClientType ping(String playerId) throws RemoteException;

	void quit(String playerId) throws RemoteException;

	GameMeta getGameMeta() throws RemoteException;

	Set<String> getPlayerSet() throws RemoteException;

	ServerInfo getServerInfo() throws RemoteException;
}
