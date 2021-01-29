package fi.utu.tech.ringersClockServer;

import fi.utu.tech.ringersClock.entities.ServerCall;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerSocketListener extends Thread {

	private String host;
	private static ServerSocketListener instance;
	private int port;
	private WakeUpService wus;
	private ServerSocket serverSocket;
	private Map<Integer,ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<Integer,ClientHandler>());
	private Thread thread;

	public ServerSocketListener(String host, int port, WakeUpService wus) {
		this.host = host;
		this.port = port;
		this.wus = wus;

		ServerSocketListener.instance = this;
	}

	public void run() {
		try {
			this.serverSocket = new ServerSocket(port);
			System.out.println("Server is online");
		} catch (IOException e) { e.printStackTrace(); }
		while (!serverSocket.isClosed()) {
			try {
				Socket clientSocket = serverSocket.accept();
				var ch = new ClientHandler(clientSocket, wus);
				clientHandlers.put(clientSocket.getPort(), ch);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void sendCallToEveryClient(ServerCall<?> cmd) {
		for(var entry : instance.clientHandlers.entrySet()) {
			entry.getValue().send(cmd);
		}
	}

	public static void sendCallToClient(Integer client, ServerCall<?> call) {
		instance.clientHandlers.get(client).send(call);
	}

	public static void sendCallToMultipleClients(Integer[] clients, ServerCall<?> call) {
		for (var ID : clients) {
			instance.clientHandlers.get(ID).send(call);
		}
	}
}