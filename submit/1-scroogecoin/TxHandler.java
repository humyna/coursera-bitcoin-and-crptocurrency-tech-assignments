import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class TxHandler {
	private UTXOPool utxoPool;
	
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	HashSet<UTXO> claimedUtxo = new HashSet<UTXO>();
    	double inputVal = 0.0;
		double outputVal = 0.0;
		int index = 0;
		
		for(Transaction.Input in:tx.getInputs()){
			UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
			if(!utxoPool.contains(ut)){
				return false;
			}
			
			if(!Crypto.verifySignature(utxoPool.getTxOutput(ut).address,tx.getRawDataToSign(index),in.signature)){
				return false;
			}
			index++;
			
			double prevOutVal = utxoPool.getTxOutput(ut).value;
			inputVal += prevOutVal;

			if (claimedUtxo.contains(ut)) {
				return false;
			}
			claimedUtxo.add(ut);
		}
		
		for (Transaction.Output out : tx.getOutputs()) {
			if (out.value < 0.0) {
				return false;
			}
			outputVal += out.value;
		}
		if (outputVal > inputVal) {
			return false;
		}
		
		return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	HashSet<Transaction> txs = new HashSet<Transaction>(Arrays.asList(possibleTxs));
    	ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
    	HashSet<Transaction> removeTxs = new HashSet<Transaction>();
    	
    	for (Transaction tx : txs) {
			if(!isValidTx(tx)) {
				continue;
			}

			validTxs.add(tx);
			updatePool(tx);
			removeTxs.add(tx);
    	}
    	for (Transaction tx : removeTxs){
			txs.remove(tx);
		}
    	return validTxs.toArray(new Transaction[validTxs.size()]);
    }
    
    private void updatePool(Transaction tx){
    	for(Transaction.Input input : tx.getInputs()) {
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			this.utxoPool.removeUTXO(utxo);
		}

		byte[] txHash = tx.getHash();
		int index = 0;
		for (Transaction.Output output : tx.getOutputs()) {
			UTXO utxo = new UTXO(txHash, index);
			index++;
			this.utxoPool.addUTXO(utxo,output);
		}
    }
}
