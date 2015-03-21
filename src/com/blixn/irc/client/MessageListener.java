package com.blixn.irc.client;

/**
 * Created by emini on 17/09/14.
 * Copyright and stuff, you know!
 */
public interface MessageListener {

    public void messageRecieved(String message);
    public void errorOccured(String errorMessage);

}
