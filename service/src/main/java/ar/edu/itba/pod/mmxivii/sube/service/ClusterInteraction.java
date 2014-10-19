package ar.edu.itba.pod.mmxivii.sube.service;

import org.jgroups.*;

import javax.annotation.Nonnull;
import java.rmi.server.UID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Pablo on 18/10/14.
 */
class ClusterInteraction extends ReceiverAdapter {

    private JChannel channel;
    private Address myAddress;
    private CardServiceImpl cardService;
    private final String CLUSTER_NAME = "SERVICE_CLUSTER";

    public ClusterInteraction(@Nonnull CardServiceImpl cardService) throws Exception {
        System.out.println("Running...");
        this.cardService = cardService;
        channel = new JChannel();
        channel.setReceiver(this);
        connect(CLUSTER_NAME);
    }

    public void connect(String clusterName) throws Exception {
        System.out.println("Connecting to " + clusterName + "...");

        channel.connect(clusterName);
        myAddress = channel.getAddress();
    }

    public void disconnect() {
        System.out.println("Disconnecting...");
        channel.disconnect();
        channel.close();
    }

    public void viewAccepted(View new_view) {
        System.out.println("Members: " + new_view.getMembers());
    }

    public void receive(Message msg) {
        if(!myAddress.equals(msg.getSrc())) {
	    	System.out.println(msg.getSrc() + ": " + msg.getObject());
            try {
                Operation operation = (Operation) msg.getObject();

                if(operation != null && (operation instanceof Operation)) {
                    ConcurrentHashMap<UID, CardState> cardStates = cardService.getCardStates();
                    CardState cardState = cardStates.get(operation.getCardId());
                    cardState.setAmount(cardState.getAmount() + operation.getAmount());
                    if(operation.getTimestamp().after(cardState.getTimestamp())) {
                        cardState.setTimestamp(operation.getTimestamp());
                    }
                }
            } catch(ClassCastException e) {
                System.out.println("Me mandaron cualquier cosa: " + e.getLocalizedMessage());
            }
        }
    }

    public void send(Operation operation) {
        Message msg = new Message(null, null, operation);
        try {
            channel.send(msg);
        } catch(Exception e) {
            System.out.println("Could not send operation: " + operation);
        }
    }

    public JChannel getChannel() {
        return channel;
    }

}