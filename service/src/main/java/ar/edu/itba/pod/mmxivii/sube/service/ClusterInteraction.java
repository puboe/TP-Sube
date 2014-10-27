package ar.edu.itba.pod.mmxivii.sube.service;

import org.jgroups.*;
import org.jgroups.blocks.locking.LockService;

import javax.annotation.Nonnull;

import java.rmi.server.UID;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Created by Pablo on 18/10/14.
 */
class ClusterInteraction extends ReceiverAdapter {

    private JChannel channel;
    private Address myAddress;
    private Set<Address> addresses = new HashSet<Address>();
    private CardServiceImpl cardService;
    private final String CLUSTER_NAME = "SERVICE_CLUSTER";
    private LockService lockService;
    private Lock leaderLock;
    private Thread racingThread;
    
    public ClusterInteraction(@Nonnull CardServiceImpl cardService) throws Exception {
        System.out.println("Running...");
        this.cardService = cardService;
        channel = new JChannel(ClusterInteraction.class.getClassLoader().getResourceAsStream("udp.xml"));
        channel.setReceiver(this);
        connect(CLUSTER_NAME);
        lockService = new LockService(channel);
        raceForLeader();
    }

    public void connect(String clusterName) throws Exception {
        System.out.println("Connecting to " + clusterName + "...");

        channel.connect(clusterName);
        myAddress = channel.getAddress();
        addresses.add(myAddress);
    }

    public void disconnect() {
        System.out.println("Disconnecting...");
        channel.disconnect();
        channel.close();
    }

    public void viewAccepted(View new_view) {
        List<Address> members = new_view.getMembers();
        System.out.println("Members: " + members);
        for(Address member: members) {
            if(!addresses.contains(member)) {
                addresses.add(member);
                if(cardService.isLeader()) {
                    updateNewServiceInstance(member);
                }
            }
        }
    }

    public void receive(Message msg) {
        if(!myAddress.equals(msg.getSrc())) {
            System.out.println(msg.getSrc() + ": " + msg.getObject());
            try {
                Operation operation = (Operation) msg.getObject();

                if (operation != null && (operation instanceof Operation)) {
                    ConcurrentHashMap<UID, CardState> cardStates = cardService.getCardStates();
                    if (!cardStates.containsKey(operation.getCardId())) {
                        cardStates.put(operation.getCardId(), new CardState(0, Calendar.getInstance().getTime()));
                    }
                    CardState cardState = cardStates.get(operation.getCardId());
                    cardState.setAmount(cardState.getAmount() + operation.getAmount());
                    if (operation.getTimestamp().after(cardState.getTimestamp())) {
                        cardState.setTimestamp(operation.getTimestamp());
                    }
                }
            } catch (ClassCastException e) {
                System.out.println("Me mandaron cualquier cosa: " + e.getLocalizedMessage());
            }
        }
    }

    public void send(Operation operation) {
       send(operation, null);
    }

    private void send(Operation operation, Address address) {
        Message msg = new Message(address, null, operation);
        try {
            channel.send(msg);
        } catch(Exception e) {
            System.out.println("Could not send operation: " + operation);
        }
    }

    private void raceForLeader() {
    	racingThread = new Thread() {
	    	@Override
	    	public void run()
	    	{
	    		leaderLock = lockService.getLock("leader");
	        	leaderLock.lock();
	        	cardService.setLeader(true);
	        	System.out.println("I am the leader.");
	    	}
    	};
    	racingThread.setDaemon(true);
    	racingThread.start();
    }

    private void updateNewServiceInstance(Address address) {
        System.out.println("Updating " + address.toString());
        for(UID cardId: cardService.getCardStates().keySet()) {
            CardState cardState = cardService.getCardStates().get(cardId);
            Operation operation = new Operation(cardId, cardState.getAmount(), cardState.getTimestamp());
            send(operation, address);
        }
    }
}