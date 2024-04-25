import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Learner {
    private final Map<Integer, String> chosenValues; // Tracks proposal numbers to chosen values
    private final KVSInterface keyValueStore; // Interface to the key-value store for applying changes

    public Learner(KVSInterface keyValueStore) {
        this.keyValueStore = keyValueStore;
        this.chosenValues = new ConcurrentHashMap<>();
    }

    // This function is called when a value has been chosen (i.e., accepted by a majority)
    public void learn(int proposalNumber, String key, String value) {
        if (!chosenValues.containsKey(proposalNumber)) { // Only learn once per proposal
            chosenValues.put(proposalNumber, value);
            applyToStore(key, value);
        }
    }

    private void applyToStore(String key, String value) {
        try {
            // Perform the operation based on some internal logic or previously known context
            if (value != null) {
                keyValueStore.PUT(key, value); // Assuming non-null value means PUT
            } else {
                keyValueStore.DELETE(key); // Assuming null value means DELETE
            }
        } catch (Exception e) {
            System.err.println("Failed to apply value to the store: " + e.getMessage());
        }
    }
}
