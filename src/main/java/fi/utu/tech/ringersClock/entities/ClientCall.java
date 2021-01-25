package fi.utu.tech.ringersClock.entities;

import java.io.Serializable;

public class ClientCall<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    private ClientCallType call;
    private T payload;

    public ClientCall(ClientCallType call, T payload){
        this.call = call;
        this.payload = payload;
    }

    public ClientCall(ClientCallType call) {
        this.call = call;
        this.payload = null;
    }

    public ClientCallType getCall() { return this.call; }

    public T getPayload() { return this.payload; }

}
