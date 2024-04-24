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

    public void propose(String value) {
        lastProposalNumber++; // Ensure a unique, incrementing proposal number
        Proposal proposal = new Proposal(lastProposalNumber, value);
        sendProposalToAcceptors(proposal);
    }

    // Send the proposal to all acceptors
    private void sendProposalToAcceptors(Proposal proposal) {
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
            if (acceptor.receiveProposal(proposal)) {
                promiseCount++;
            }
        }
        if (promiseCount >= majority) {
            // If enough promises are received, consider this proposal accepted by the quorum
            commitProposal(proposal);
        }
    }

    // Method to commit the proposal
    private void commitProposal(Proposal proposal) {
        int commitCount = 0;  // To count how many acceptors have successfully committed the proposal
        for (Acceptor acceptor : acceptors) {
            try {
                acceptor.acceptProposal(proposal);  // Assuming this method could throw an exception if commit fails
                commitCount++;  // Increment only if acceptProposal does not throw an exception
            } catch (Exception e) {
                System.out.println("Error during commit on acceptor: " + e.getMessage());
                // Optionally implement retry logic here or just continue to try with other acceptors
            }
        }
        // Check if the commit was successful on a majority of acceptors
        if (commitCount >= majority) {
            System.out.println("Proposal " + proposal.getProposalNumber() + " committed with value: " + proposal.getProposedValue());
        } else {
            System.out.println("Failed to achieve majority commit for proposal " + proposal.getProposalNumber());
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
