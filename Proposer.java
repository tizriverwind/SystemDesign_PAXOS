import java.util.List;
// import java.rmi.Naming; // if using RMI for communication

public class Proposer {

    private int lastProposalNumber = 0;
    private final List<Acceptor> acceptors; // List of acceptor objects directly
    private final int majority; // Majority size needed to accept a proposal

    public Proposer(List<Acceptor> acceptors, int majority) {
        this.acceptors = acceptors;
        this.majority = (acceptors.size()/2) + 1; // Need 3 to reach consensus
    }

    public void propose(String key, String value) {
        lastProposalNumber++; // Ensure a unique, incrementing proposal number
        Message proposalMessage = new Message(Message.MessageType.PROPOSE, lastProposalNumber, key, value);
        sendProposalToAcceptors(proposalMessage);
    }

    // Send the proposal to all acceptors
    private void sendProposalToAcceptors(Message message) {
        int promiseCount = 0;
        // for (Acceptor acceptor : acceptors) {
        //     try {  
        //         Acceptor acceptor = (Acceptor) Naming.lookup(address);
        //         boolean promise = acceptor.receiveProposal(proposal.getProposalNumber(), proposal.getProposedValue());
        //         if (promise) {
        //             promiseCount++;
        //         }

        //     } catch (Exception e) {
        //         System.out.println("Error sending proposal to Acceptor: " + e.getMessage());
        //     }
        // }
        for (Acceptor acceptor : acceptors) {
            if (acceptor.receiveProposal(message)) {
                promiseCount++;
            }
        }
        if (promiseCount >= majority) {
            // If enough promises are received, consider this proposal accepted by the quorum
            commitProposal(message);
        }
    }

    public static class Message {
        public enum MessageType { PROPOSE, PROMISE, ACCEPT_REQUEST, ACCEPTED}
        private MessageType type;
        private int proposalNumber;
        private String key;
        private String value;

        public Message(MessageType type, int proposalNumber, String key, String value) {
            this.type = type;
            this.proposalNumber = proposalNumber;
            this.key = key;
            this.value = value;
        }

        public MessageType getType() { return type; }
        public int getProposalNumber() { return proposalNumber; }
        public String getKey() { return key; }
        public String getValue() { return value; }

    }

    
    // Method to commit the proposal
    private void commitProposal(Message message) {
        message = new Message(Message.MessageType.ACCEPT_REQUEST, message.getProposalNumber(), message.getKey(), message.getValue());
        int commitCount = 0;  // To count how many acceptors have successfully committed the proposal
        for (Acceptor acceptor : acceptors) {
            try {
                if (acceptor.acceptProposal(message)) {
                    commitCount++;  // Increment only if acceptProposal does not throw an exception
                }       
            } catch (Exception e) {
                System.out.println("Error during commit on acceptor: " + e.getMessage());
                // Optionally implement retry logic here or just continue to try with other acceptors
            }
        }
        // Check if the commit was successful on a majority of acceptors
        if (commitCount >= majority) {
            System.out.println("Commit successful for proposal number " + message.getProposalNumber() + ", committed with value: " + message.getValue());
        } else {
            System.out.println("Commit failed to achieve for proposal number " + message.getProposalNumber());
            // Handle the failure case, perhaps by retrying the proposal or aborting the process
        }
    }

    
    public static class Proposal {
        private final int proposalNumber;
        private final String proposedValue;

        public Proposal(int proposalNumber, String proposedValue) {
            this.proposalNumber = proposalNumber;
            this.proposedValue = proposedValue;
        }

        public int getProposalNumber() {
            return proposalNumber;
        }

        public String getProposedValue() {
            return proposedValue;
        }
    }
    
}
