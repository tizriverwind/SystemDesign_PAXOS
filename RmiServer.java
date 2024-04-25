import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class RmiServer {
    private ArrayList<KVSInterface> serverStubs;
    private Registry registry;
    private static Proposer proposer; // Assuming you have a way to initialize and manage Proposer, Acceptor, and Learner

    public RmiServer(Proposer proposer) {
        this.serverStubs = new ArrayList<>();
        //this.proposer = proposer;
    }

    public static void main(String[] args) throws RemoteException {
        Proposer proposer = initializeProposer();
        RmiServer server = new RmiServer(proposer); 
        server.startServers();
    }
    
    private static  Proposer initializeProposer() {
        ArrayList<Acceptor> acceptors = new ArrayList<>();
        // Initialize acceptors and add them to the list
        for (int i = 0; i < 5; i++) {
            Acceptor acceptor = new Acceptor(); 
            acceptors.add(acceptor);
        }
        int majority = 5 / 2 + 1;
        // Initialize the proposer with the list of acceptors and the majority required
        proposer = new Proposer(acceptors, majority);
        return proposer;
    }
    

    public void startServers() {
        int port = 10000;
        try {
            registry = LocateRegistry.createRegistry(port);
            for (int i = 0; i < 5; i++) {
                KVSImpl obj = new KVSImpl(proposer);
                String serviceName = "RmiMap" + i;
                registry.rebind(serviceName, obj);
                System.out.println("RmiServer: Service " + serviceName + " ready on port " + port);
                serverStubs.add(obj);
            }
        } catch (Exception e) {
            System.err.println("Exception while starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

}