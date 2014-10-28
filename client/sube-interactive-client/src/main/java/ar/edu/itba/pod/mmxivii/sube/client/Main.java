package ar.edu.itba.pod.mmxivii.sube.client;

import ar.edu.itba.pod.mmxivii.sube.common.*;

import javax.annotation.Nonnull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.*;

public class Main extends BaseMain {

	private CardClient cardClient = null;
    private Map<String, Card> map = new HashMap<String, Card>();

	private Main(@Nonnull String[] args) throws NotBoundException {

        super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
	}

	public static void main(@Nonnull String[] args ) throws Exception {

		final Main main = new Main(args);
		main.run();
	}

	private void run() throws IOException {

        System.out.println("Main.run");
        System.out.print("Ingrese su nombre: ");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String name = buffer.readLine();
        System.out.println("Bienvenido " + name + "!");
        System.out.println("Ingrese comandos: ");

        String line;
        do {
            System.out.print("> ");
            line = buffer.readLine();
            if(line.startsWith("newcard")) {    // newcard nombretarjeta
                String[] args = line.split("\\s+");
                if(args[1] == null) {
                    System.out.println("Falta argumento.");
                } else {
                    Card card = cardClient.newCard(name, args[1]);
                    map.put(args[1], card);
                    System.out.println("Tarjeta " + args[1] + " creada.");
                }

            } else if(line.startsWith("recharge")) {    // recharge tarjeta descripcion monto
                String[] args = line.split("\\s+");
                if(args[1] == null || args[2] == null || args[3] == null) {
                    System.out.println("Faltan argumentos.");
                } else {
                    try {
                        if (!map.containsKey(args[1])) {
                            System.out.println("Tarjeta inexistente");
                        } else {
                            Double amount = Double.valueOf(args[3]);
                            Double response = cardClient.recharge(map.get(args[1]).getId(), args[2], amount);
                            printResponse(response);
                        }
                    } catch(NumberFormatException e) {
                        System.out.println("El monto debe ser un numero.");
                        e.printStackTrace();
                    }
                }
            } else if(line.startsWith("travel")) {    // travel tarjeta descripcion monto
                String[] args = line.split("\\s+");
                if(args[1] == null || args[2] == null || args[3] == null) {
                    System.out.println("Faltan argumentos.");
                } else {
                    try {
                        if (!map.containsKey(args[1])) {
                            System.out.println("Tarjeta inexistente");
                        } else {
                            Double amount = Double.valueOf(args[3]);
                            Double response = cardClient.travel(map.get(args[1]).getId(), args[2], amount);
                            printResponse(response);
                        }
                    } catch(NumberFormatException e) {
                        System.out.println("El monto debe ser valido.");
                    }
                }
            } else if(line.startsWith("balance")) {    // balance tarjeta
                String[] args = line.split("\\s+");
                if(args[1] == null) {
                    System.out.println("Falta argumento.");
                } else if(!map.containsKey(args[1])) {
                    System.out.println("Tarjeta inexistente");
                } else {
                    Double balance = cardClient.getCardBalance(map.get(args[1]).getId());
                    printResponse(balance);
                }
            } else if(line.equals("help")) {
                printHelp();
            } else if(line.equals("getcards")) {
                System.out.println("Tarjetas disponibles:");
                for (String card : map.keySet()) {
                    System.out.println(" - " + card);
                }
            } else if(line.equals("x")) {
                System.exit(0);
            } else {
                System.out.println("Comando no disponible.");
            }
        } while(!"x".equals(line));
	}

    private void printResponse(Double response) {
        switch (response.intValue()) {
            case -1:
                System.out.println("Tarjeta no encontrada.");
                break;
            case -2:
                System.out.println("No se pudo procesar el pedido.");
                break;
            case -3:
                System.out.println("Falla de comunicacion");
                break;
            case -4:
                System.out.println("Operacion no permitida.");
                break;
            case -5:
                System.out.println("Timeout del servicio.");
                break;
            default:
                System.out.println("$" + response);
        }
    }

    private void printHelp() {
        System.out.println("Comandos disponibles:");
        System.out.println(" - getcards");
        System.out.println(" - newcard <nombre_tarjeta>");
        System.out.println(" - balance <nombre_tarjeta>");
        System.out.println(" - recharge <nombre_tarjeta> <descripcion> <monto>");
        System.out.println(" - travel <nombre_tarjeta> <descripcion> <monto>");
        System.out.println(" - exit: x");
    }
}
