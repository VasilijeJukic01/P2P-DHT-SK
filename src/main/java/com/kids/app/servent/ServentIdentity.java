package com.kids.app.servent;

import java.io.Serial;
import java.io.Serializable;

public record ServentIdentity(
        String ip,
        int port
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 6342891038472613908L;

    @Override
    public String toString() {
        return ip + ":" + port;
    }

}
