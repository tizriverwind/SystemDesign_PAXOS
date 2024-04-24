public class Acceptor {
    private int highestProposalNumber = 0;
    private String acceptedValue = null;
    private int acceptedProposalNumber = -1;

    public Acceptor() {
        // Initialize any necessary states
    }

    /**
     * Receives a proposal from a proposer.
     * @param proposal The proposal being received.
     * @return true if the proposal is accepted, false otherwise.
     */
    public synchronized boolean receiveProposal(Proposer.Proposal proposal) {
        // Check if the received proposal's number is higher than any previously received
        if (proposal.getProposalNumber() > highestProposalNumber) {
            highestProposalNumber = proposal.getProposalNumber();  // Update the highest proposal number seen so far
            // Optionally, you might want to log or perform other actions here
            return true;  // Promise to not accept any earlier proposals
        }
        return false;  // Reject if an equal or higher proposal has been seen
    }

    /**
     * Accepts the proposal if it matches the highest proposal number promised.
     * @param proposal The proposal to be accepted.
     */
    public synchronized void acceptProposal(Proposer.Proposal proposal) {
        // Accept the proposal only if it matches the highest proposal number promised
        if (proposal.getProposalNumber() == highestProposalNumber) {
            acceptedValue = proposal.getProposedValue();  // Set the value as accepted
            acceptedProposalNumber = proposal.getProposalNumber();  // Record which proposal number was accepted
            System.out.println("Accepted proposal number " + acceptedProposalNumber + " with value: " + acceptedValue);
        } else {
            System.out.println("Failed to accept proposal number " + proposal.getProposalNumber() + " as it does not match the highest promised proposal number " + highestProposalNumber);
        }
    }

    // Optional: method to get the current state of the acceptor
    public synchronized String getAcceptedValue() {
        return acceptedValue;
    }

    public synchronized int getAcceptedProposalNumber() {
        return acceptedProposalNumber;
    }
}
