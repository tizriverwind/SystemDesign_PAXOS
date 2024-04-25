

public class Acceptor {
    private int highestProposalNumber = 0;
    private String acceptedValue = null;
    private int acceptedProposalNumber = -1;

    public Acceptor() {
        // Initialize any necessary states
    }

    /**
     * Receives a proposal from a proposer and promises to accept if the proposal number is higher.
     * @param message The proposal message being received.
     * @return true if the proposal is higher and accepted for promise, false otherwise.
     */
    public synchronized boolean receiveProposal(Proposer.Message message) {
        // Check if the received proposal's number is higher than any previously received
        if (message.getType() == Proposer.Message.MessageType.PROMISE && message.getProposalNumber() > highestProposalNumber) {
            highestProposalNumber = message.getProposalNumber(); 
            /// sendMessage(new Proposer.Message(Proposer.Message.MessageType.PROMISE, message.getProposalNumber(), message.getKey(), null));
            return true;  // Promise to not accept any earlier proposals
        }
        return false;  // Reject if an equal or higher proposal has been seen
    }

    /**
     * Accepts the proposal if it matches the highest proposal number promised.
     * @param message The accept_request message to be accepted.
     * @return true if the proposal was accepted, false otherwise.
     */
    public synchronized boolean acceptProposal(Proposer.Message message) {

        if (message.getType() == Proposer.Message.MessageType.ACCEPT_REQUEST && message.getProposalNumber() == highestProposalNumber) {
            acceptedValue = message.getValue(); // Set the value as accepted value
            acceptedProposalNumber = message.getProposalNumber();
        // if (proposal.getProposalNumber() == highestProposalNumber) {
        //     acceptedValue = proposal.getProposedValue();  // Set the value as accepted
        //     acceptedProposalNumber = proposal.getProposalNumber();  // Record which proposal number was accepted
            System.out.println("Accepted proposal number " + acceptedProposalNumber + " with value: " + acceptedValue);
            return true;
        } else {
            System.out.println("Failed to accept proposal number " + message.getProposalNumber() + " as it does not match the highest promised proposal number " + highestProposalNumber);
            return false;
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