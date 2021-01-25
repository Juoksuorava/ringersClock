package fi.utu.tech.ringersClock.entities;

import java.io.Serializable;

public class ServerCall<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    private ServerCallType call;
    private T payload;


    public ServerCall(ServerCallType call, T payload) {
        this.call = call;
        this.payload = payload;
    }

    public ServerCall(ServerCallType call) {
        this.call = call;
        this.payload = null;
    }


    public ServerCallType getCall() { return this.call; }

    public T getPayload() { return this.payload; }
}
