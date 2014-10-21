package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;

import javax.annotation.Nonnull;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_SERVICE_REGISTRY_BIND;

// Run with -Djava.net.preferIPv4Stack=true
public class Main extends BaseMain {

	private final CardServiceRegistry cardServiceRegistry;
	private CardServiceImpl cardService = null;

	private Main(@Nonnull String[] args) throws RemoteException, NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		final CardRegistry cardRegistry = Utils.lookupObject(CARD_REGISTRY_BIND);
		cardServiceRegistry = Utils.lookupObject(CARD_SERVICE_REGISTRY_BIND);
        try {
            cardService = new CardServiceImpl(cardRegistry);
        } catch (Exception e) {
            System.out.println("Could not start service. Exiting...");
            System.exit(1);
        }
	}

	public static void main(@Nonnull String[] args) throws Exception {
		final Main main = new Main(args);
		main.run();
	}

	private void run() throws RemoteException {
		cardServiceRegistry.registerService(cardService);

		System.out.println("Starting Service!");
		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			line = scan.next();
            printBalance();
		} while(!"x".equals(line));
        cardServiceRegistry.unRegisterService(cardService);
        cardService.finalize();
		System.out.println("Service exit.");
		System.exit(0);
	}

    private void printBalance() {
        ConcurrentHashMap<UID, CardState> map = cardService.getCardStates();
        for(UID card: map.keySet()) {
            System.out.println(card + ": " + map.get(card));
        }
    }
}

