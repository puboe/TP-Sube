package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.*;

import javax.annotation.Nonnull;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

public class CardServiceImpl extends UnicastRemoteObject implements CardService {

	private static final long serialVersionUID = 2919260533266908792L;
	@Nonnull
	private final CardRegistry cardRegistry;
    private ConcurrentHashMap<UID, CardState> cardStates = new ConcurrentHashMap<UID, CardState>();
    private ClusterInteraction clusterInteraction;
    private boolean leader = false;

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

        double result = validateOperation(id, amount * -1);
        if(result > 0) {
            clusterInteraction.send(new Operation(id, amount * -1));
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

        if(Math.abs(amount) > 100 || Math.abs(amount) < 1) {
            return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
        }

        if(!getCardStates().containsKey(id)) {
            getCardStates().put(id, new CardState(0, Calendar.getInstance().getTime()));
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

    public double downloadToServer() {
        try {
            for (UID cardId : cardStates.keySet()) {
                double balance = cardRegistry.getCardBalance(cardId);
                CardState cardState = cardStates.get(cardId);
                double operationAmount = cardState.getAmount() - balance;
                if(operationAmount != 0) {
                    System.out.println("Downloading operation. UID: " + cardId + ", amount: $" + operationAmount);
                    cardRegistry.addCardOperation(cardId, "Operation", operationAmount);
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Could not download to server.");
            e.printStackTrace();
        }
        return CardRegistry.CANNOT_PROCESS_REQUEST;
    }

    public void setLeader(boolean isLeader) {
        this.leader = isLeader;
    }

    public boolean isLeader() {
        return leader;
    }
}
