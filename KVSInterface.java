import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVSInterface extends Remote {
    
    String PUT(String key, String value) throws RemoteException;
    String GET(String key) throws RemoteException;
    String DELETE(String key) throws RemoteException;

    /**
     * Prepares the server for an operation (PUT or DELETE) by acquiring a lock and checking for conflicts.
     *
     * @param key The key to be put or deleted.
     * @param value The value to be associated with the key (ignored for DELETE operation).
     * @param operation The type of operation ("put" or "delete").
     * @return {@code true} if the operation can proceed (no conflicts and lock acquired),
     *         {@code false} otherwise.
     * @throws RemoteException if a remote communication error occurs.
     */
    public boolean prepareToOperation(String key, String value, String operation) throws RemoteException;

    /**
     * Commits an update to the dictionary by performing the actual PUT or DELETE operation,
     * and then releases the lock associated with the key.
     *
     * @param key The key to be put or deleted.
     * @param value The value to be associated with the key (ignored for DELETE operation).
     * @param operation The type of operation ("put" or "delete").
     * @throws RemoteException if a remote communication error occurs.
     */
    void allServerUpdate(String key, String value, String operation)throws RemoteException;

    boolean propose(int proposalId, String key, String value) throws RemoteException;
    boolean promise(int proposalId, String key, String value) throws RemoteException;
    boolean acceptRequest(int proposalId, String key, String value) throws RemoteException;
    void accepted(int proposalId, String key, String value) throws RemoteException;

}
