import java.util.HashSet;
import java.util.Set;

public class MalDoNothing implements Node {

	public MalDoNothing(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }
	
	@Override
	public void setFollowees(boolean[] followees) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPendingTransaction(Set<Transaction> pendingTransactions) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Transaction> sendToFollowers() {
		// TODO Auto-generated method stub
		return new HashSet<Transaction>();
	}

	@Override
	public void receiveFromFollowees(Set<Candidate> candidates) {
		// TODO Auto-generated method stub

	}

}
