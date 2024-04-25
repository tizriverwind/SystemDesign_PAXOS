import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVSInterface extends Remote {
    
    String PUT(String key, String value) throws RemoteException;
    String GET(String key) throws RemoteException;
    String DELETE(String key) throws RemoteException;
}

