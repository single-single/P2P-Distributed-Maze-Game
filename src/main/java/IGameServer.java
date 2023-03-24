import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IGameServer extends Remote {
    GameStateData getGameData() throws RemoteException;
    GameStateData addPlayer(String playerId) throws RemoteException;
    GameStateData movePlayer(String playerId, Direction direction) throws RemoteException;
    void changeBackupServer(String backupServer) throws RemoteException;
    void removePlayer(String playerId) throws RemoteException;
}
