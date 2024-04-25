import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KVSImpl extends UnicastRemoteObject implements KVSInterface {
    private ConcurrentHashMap<String, String> map;
    private ConcurrentHashMap<String, ReentrantLock> locks;
    private Proposer proposer;

    public KVSImpl(Proposer proposer) throws RemoteException {
        super();
        this.map = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
        this.proposer = proposer;
    }

    @Override
    public String PUT(String key, String value) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            if (map.containsKey(key)) {
                lock.unlock();
                return "Error: Key already exists. " + sdf.format(new Date());
            }
            // Assuming your Proposer has a method to create and send proposals correctly
            boolean accepted = proposer.propose(key, value); // Adjust to your Proposer's actual method signature
            if (accepted) {
                map.put(key, value);  // Assume immediate local update; actual application should be handled by Learner
                return "PUT successful for key " + key + ". " + sdf.format(new Date());
            } else {
                return "PUT operation failed. " + sdf.format(new Date());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String DELETE(String key) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!map.containsKey(key)) {
                lock.unlock();
                return "Error: Key does not exist. " + sdf.format(new Date());
            }
            // Adjust to your Proposer's actual method signature
            boolean accepted = proposer.propose(key, null); // Treat null value as a delete request
            if (accepted) {
                map.remove(key);  // Assume immediate local update; actual application should be handled by Learner
                return "DELETE successful for key " + key + ". " + sdf.format(new Date());
            } else {
                return "DELETE operation failed. " + sdf.format(new Date());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String GET(String key) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String value = map.get(key);
        if (value != null) {
            return "Value for key " + key + " is " + value + ". " + sdf.format(new Date());
        } else {
            return "Error: Key not found. " + sdf.format(new Date());
        }
    }
}
