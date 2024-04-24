import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Learner {
    private final int majorityThreshold;
    private final Map<Integer, ProposalCount> acceptedProposals;
    private final KVSInterface keyValueStore;

    public Learner(int majorityThreshold, KVSInterface keyValueStore) {
        this.majorityThreshold = majorityThreshold;
        this.keyValueStore = keyValueStore;
        this.acceptedProposals = new ConcurrentHashMap<>();
    }

    public void notifyAccepted(int proposalNumber, String key, String value) {
        ProposalCount count = acceptedProposals.getOrDefault(proposalNumber, new ProposalCount(key, value));
        count.incrementCount();

        if (count.getCount() >= majorityThreshold) {
            if (applyValueToStore(key, value)) {
                System.out.println("Value " + value + " for key " + key + " has been committed to the store.");
                acceptedProposals.remove(proposalNumber);  // Clean up after applying the value
            }
        } else {
            acceptedProposals.put(proposalNumber, count);
        }
    }

    private boolean applyValueToStore(String key, String value) {
        try {
            keyValueStore.PUT(key, value);  // Assuming PUT is used for simplicity; in real scenarios, this might depend on the operation type (PUT, DELETE, etc.)
            return true;
        } catch (Exception e) {
            System.err.println("Failed to apply value to the store: " + e.getMessage());
            return false;
        }
    }

    private static class ProposalCount {
        private final String key;
        private final String value;
        private int count;

        public ProposalCount(String key, String value) {
            this.key = key;
            this.value = value;
            this.count = 0;
        }

        public void incrementCount() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
