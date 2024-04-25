import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
// import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
// import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class KVSImpl extends UnicastRemoteObject implements KVSInterface {

    private ConcurrentHashMap<String, String> map;
    private ConcurrentHashMap<String, ReentrantLock> locks;
    private RmiServer server;
    private int port;

    // Paxos-related states
    private int highestProposedNumber = 0;
    private int highestAcceptedNumber = 0;
    private String acceptedValue = null;
    private final HashMap<String, Integer> proposalCounts = new HashMap<>();

    /**
     * Constructor for KVSImpl.
     *
     * @throws RemoteException If a remote method call fails.
     */
    public KVSImpl(RmiServer server, int port) throws RemoteException {
        super();
        map = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();
        this.server = server;
        this.port = port;
    }
    /**
     * Prepares the server for an operation (PUT or DELETE) by acquiring a lock and checking for conflicts.
     *
     * @param key The key to be PUT or deleted.
     * @param value The value to be associated with the key (ignored for DELETE operation).
     * @param operation The type of operation ("PUT" or "delete").
     * @return {@code true} if the operation can proceed (no conflicts and lock acquired),
     *         {@code false} otherwise.
     * @throws RemoteException if a remote communication error occurs.
     */
    public boolean prepareToOperation(String key, String value, String operation) throws RemoteException {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        if (lock.tryLock()) {
            if ("PUT".equals(operation) && map.containsKey(key)) {
                System.out.println("port "+port+" already has the key, refuse to PUT.");
                lock.unlock();
                return false;
            } else if ("DELETE".equals(operation) && !map.containsKey(key)) {
                System.out.println("port "+port+" has no such key, refuse to delete.");
                lock.unlock();
                return false;
            }
            return true;
        } else {
            System.out.println("port "+port+" because of some reason, maybe lock, can't PUT.");
            return false;
        }
    }
    /**
     * Commits an update to the map by performing the actual PUT or DELETE operation,
     * and then releases the lock associated with the key.
     *
     * @param key The key to be PUT or deleted.
     * @param value The value to be associated with the key (ignored for DELETE operation).
     * @param operation The type of operation ("PUT" or "delete").
     * @throws RemoteException if a remote communication error occurs.
     */
    public void allServerUpdate(String key, String value, String operation) throws RemoteException {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                if ("PUT".equals(operation)) {
                    map.put(key, value);
                } else if ("DELETE".equals(operation)) {
                    map.remove(key);
                }
            } finally {
                lock.unlock();
            }
        }
    }
    /**
     * PUTs a key-value pair into the map.
     *
     * @param key The key to be added or updated.
     * @param value The value associated with the key.
     * @return A success message if the operation is successful, or an error message if the key already exists.
     * @throws RemoteException If a remote method call fails.
     */
    public String PUT(String key, String value) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            if (!prepareToOperation(key, value, "PUT")) {
                return "KVSImpl on port " + port + " error: Not allowed by self-checking. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
            } else if (!server.askAllServer(key, value, "PUT")) {
                return "KVSImpl on port " + port + " error: Not allowed by other server. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
            }
            server.allServerDoRealUpdate(key, value, "PUT");

        } catch (RemoteException e) {
            //e.printStackTrace();
            return "KVSImpl on port " + port + " error: RemoteException occurred. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
        }
        return "KVSImpl: PUT success for key (" + key + ") Current time: " + sdf.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Retrieves the value associated with a key from the map.
     *
     * @param key The key whose value is to be retrieved.
     * @return The value associated with the key, or an error message if the key is not found.
     * @throws RemoteException If a remote method call fails.
     */
    @Override
    public String GET(String key) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //System.out.println("Current time: " + sdf.format(new Date(System.currentTimeMillis())));
        if (!map.containsKey(key)){
            return "KVSImpl get error: There is no such key."
                    + "Current time: " + sdf.format(new Date(System.currentTimeMillis()));
        }
        return "KVSImpl: GET success for key (" + key + ")" + " with value (" + map.get(key) + ")"
                + "Current time: " + sdf.format(new Date(System.currentTimeMillis()));
    }
    /**
     * DELETEs a key-value pair from the map.
     *
     * @param key The key to be deleted.
     * @return A success message if the operation is successful, or an error message if the key is not found or already deleted.
     * @throws RemoteException If a remote method call fails.
     */
    @Override
    public String DELETE(String key) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            if (!prepareToOperation(key, "", "delete")) {
                return "KVSImpl on port " + port + " DELETE error: Not allowed by self-checking. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
            } else if (!server.askAllServer(key, "", "DELETE")) {
                return "KVSImpl on port " + port + " DELETE error: Not allowed by other server. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
            }
            server.allServerDoRealUpdate(key, "", "DELETE");

        } catch (RemoteException e) {
            //e.printStackTrace();
            return "KVSImpl on port " + port + " DELETE error: RemoteException occurred. Current time: " + sdf.format(new Date(System.currentTimeMillis()));
        }
        return "KVSImpl: DELETE success for key (" + key + ") Current time: " + sdf.format(new Date(System.currentTimeMillis()));
    }


    @Override
    public boolean propose(int proposalId, String key, String value) throws RemoteException {
        // logic to handle proposal reception
        return false;
    }

    @Override
    public boolean promise(int proposalId, String key, String value) throws RemoteException {
        // Logic to send a promise
        return false; // Example return
    }

    @Override
    public boolean acceptRequest(int proposalId, String key, String value) throws RemoteException {
        // Logic to accept a proposal
        return false; // Example return
    }

    @Override
    public void accepted(int proposalId, String key, String value) throws RemoteException {
        // Logic when a proposal is accepted
    }
}
