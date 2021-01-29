package fi.utu.tech.ringersClockServer;
import fi.utu.tech.ringersClock.entities.ServerCall;
import fi.utu.tech.ringersClock.entities.ClientCall;
import fi.utu.tech.ringersClock.entities.ServerCallType;
import fi.utu.tech.ringersClock.entities.WakeUpGroup;

import java.net.*;
import java.io.Serializable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

//import fi.utu.tech.ringersClock.entities.MessagePackage;

public class ClientHandler extends Thread {

    private Thread thread;
    private Socket client;
    private MessageHandler mh;
    private WakeUpService wus;
    private InputStream inS;
    private OutputStream outS;
    private ObjectOutputStream objOutS;
    private ObjectInputStream objInS;

    public ClientHandler(Socket clientSocket, WakeUpService wus) {
        this.client = clientSocket;
        this.wus = wus;
        this.thread = new Thread(this);
        this.thread.start();
        mh = new MessageHandler();
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + client.getPort() + client.getInetAddress());

        try {
            inS = client.getInputStream();
            outS = client.getOutputStream();
            objOutS = new ObjectOutputStream(outS);
            objInS = new ObjectInputStream(inS);
            initialize();
            while (client.isConnected()) {
                    mh.handle(objInS.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Connection to client closed: " + client.getRemoteSocketAddress());
        }
    }

    private void initialize() {
        send(new ServerCall<>(
                ServerCallType.STATUS_UPDATE,"Server connection established: +" + client.getPort()));
        send(new ServerCall<>(
                ServerCallType.UPDATE_EXISTING_GROUPS, wus.getGroups()));
    }

    public void sendSerializable(Serializable msg) {
        try {
            objOutS.writeObject(msg);
            objOutS.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }
    public void send(ServerCall<?> msg) {
        sendSerializable(msg);
    }

    public void close() throws IOException {
        objInS.close();
        objOutS.close();
        client.close();
        System.out.println("Connection to client closed:" + client.getRemoteSocketAddress());
    }

    private class MessageHandler {

        private void handleAlarmAll() {
            wus.alarmAll(client.getPort());
        }

        private void handleCancelAlarm() {
            wus.cancelAlarm(client.getPort());
        }

        private void handleResignGroup(WakeUpGroup wug) {
            if (wug.getID() == client.getPort()){
                wus.removeGroup(wug);
            } else {
                wus.resignGroup(wug.getID(), client.getPort());
            }
        }

        private void handleCreateGroup(WakeUpGroup wug) {
            int ID = client.getPort();
            wus.addGroup(ID, wug);
        }

        private void handleJoinGroup(WakeUpGroup wug) {
            wus.joinGroup(wug, client.getPort());
        }

        public void handle(Object obj) {
            try {
                if (obj instanceof ClientCall) {
                    var cmd = (ClientCall<?>) obj;
                    var command = cmd.getCall();
                    var payload = cmd.getPayload();

                    switch (command) {
                        case ALARM_ALL:
                            handleAlarmAll();
                            break;

                        case CANCEL_ALARM:
                            handleCancelAlarm();
                            break;

                        case CREATE_WAKE_UP_GROUP:
                            if (payload instanceof WakeUpGroup) handleCreateGroup((WakeUpGroup) payload);
                            else throw new Exception("Client message not handled correctly: payload not in correct format");
                            break;

                        case JOIN_WAKE_UP_GROUP:
                            if (payload instanceof WakeUpGroup) handleJoinGroup((WakeUpGroup) payload);
                            else throw new Exception("Client message not handled correctly: payload not in correct format");
                            break;

                        case RESIGN_WAKE_UP_GROUP:
                            if (payload instanceof WakeUpGroup) handleResignGroup((WakeUpGroup) payload);
                            else throw new Exception("Client message not handled correctly: payload not in correct format");
                    }
                } else { throw new Exception("Client message not handled correctly: incorrect object"); }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}