import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class KVSImpl extends UnicastRemoteObject implements KVSInterface {

    private ConcurrentHashMap<String, String> dictionary;
    private ConcurrentHashMap<String, ReentrantLock> locks;
    private Proposer proposer; // This references the Proposer for initiating proposals
    private Learner learner;   // This references the Learner for applying finalized decisions

    public KVSImpl(Proposer proposer, Learner learner) throws RemoteException {
        super();
        this.dictionary = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
        this.proposer = proposer;
        this.learner = learner;
    }

    // Initiating a PUT operation through Proposer
    public String PUT(String key, String value) throws RemoteException {
        if (proposer.propose(key, value, "PUT")) {
            return "Proposal to PUT key-value pair initiated.";
        } else {
            return "Proposal to PUT key-value pair failed.";
        }
    }

    // Initiating a DELETE operation through Proposer
    public String DELETE(String key) throws RemoteException {
        if (proposer.propose(key, null, "DELETE")) {
            return "Proposal to DELETE key initiated.";
        } else {
            return "Proposal to DELETE key failed.";
        }
    }

    // Basic GET operation that doesn't involve consensus
    public String GET(String key) throws RemoteException {
        String value = dictionary.get(key);
        if (value != null) {
            return "Value retrieved: " + value;
        } else {
            return "Key not found.";
        }
    }

    // Apply changes to the store as finalized by the Paxos Learner
    protected void applyChange(String key, String value, String operation) {
        if (operation.equals("PUT")) {
            dictionary.put(key, value);
        } else if (operation.equals("DELETE")) {
            dictionary.remove(key);
        }
    }
}
