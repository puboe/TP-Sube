package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.*;

import javax.annotation.Nonnull;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class CardServiceImpl extends UnicastRemoteObject implements CardService {

	private static final long serialVersionUID = 2919260533266908792L;
	@Nonnull
	private final CardRegistry cardRegistry;
    private ConcurrentHashMap<UID, CardState> cardStates = new ConcurrentHashMap<UID, CardState>();
    private ClusterInteraction clusterInteraction;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry) throws Exception {
		super(0);
		this.cardRegistry = cardRegistry;
        this.clusterInteraction = new ClusterInteraction(this);
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException {
		return cardRegistry.getCardBalance(id);
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {

        double result = validateOperation(id, amount);
        if(result > 0) {
            clusterInteraction.send(new Operation(id, amount));
        }
		return result;
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {

        double result = validateOperation(id, amount);
        if(result > 0) {
            clusterInteraction.send(new Operation(id, amount));
        }
        return result;
	}

    private double validateOperation(UID id, double amount) {

        if(amount > 100 || amount < 1) {
            return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
        }
        CardState cardState = getCardStates().get(id);
        double balance = cardState.getAmount() + amount;
        if(balance > CardRegistry.MAX_BALANCE || balance < 0) {
            return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
        }
        return balance;
    }

    public ConcurrentHashMap<UID, CardState> getCardStates() {
        return cardStates;
    }

    public void finalize() {
        clusterInteraction.disconnect();
    }

}
