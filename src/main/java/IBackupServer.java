import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBackupServer extends Remote {
    GameStateData getGameData() throws RemoteException;
    void updateGameData(GameStateData gameStateData) throws RemoteException;
}
