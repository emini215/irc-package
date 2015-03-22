package com.blixn.irc.client;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by emini on 17/09/14.
 * Copyright and stuff, you know!
 */
class ServerListener implements Runnable {

    private Connection connection;
    private BufferedReader input;

    public ServerListener(Connection connection, BufferedReader input) {
        this.connection = connection;
        this.input = input;
    }

    @Override
    public void run() {
        while (!connection.isClosed()) {
            try {
		String message = input.readLine();
		if (message.startsWith("PING"))
		    connection.pong(message.substring(6));
		else
		    connection.messageRecieved(message);
            } catch (IOException io) {
                connection.errorOccured(io.getMessage());
            }
        }
    }
}
