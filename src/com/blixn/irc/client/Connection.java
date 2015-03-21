package com.blixn.irc.client;

import com.blixn.irc.User;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by emini on 17/09/14.
 * Copyright and stuff, you know!
 *
 * For creating and maintaining a connection to an irc-server
 * using the RFC2812 protocol.
 */
public class Connection {

    private Socket connection;
    private BufferedReader input;
    private PrintWriter output;

    private ArrayList<MessageListener> listeners;

    private User user;

    /**
     * Creates a connection object without connecting to a host.
     * Connection to host created manually with the 'connect'-method.
     */
    public Connection() {
        listeners = new ArrayList<>();
    }

    /**
     * Creates Connection object to use when dealing with connection to irc-host.
     * Initiates the connect method to setup the connection.
     *
     * @param address       the address where to find the host
     * @param port          the port where to connect to host
     * @param user          the user used to identify to host
     * @throws IOException  when a connection cannot be properly set up.
     */
    public Connection(String address, int port, User user) throws IOException {

        this();
        connect(address, port, user);
    }

    /**
     * Creates a connection to host and initializes communication using
     * irc-protocol with host. Also creates a thread to listen for messages
     * from host.
     *
     * @param address       the address where to find the host
     * @param port          the port where to connect to host
     * @param user          the user used to identify to host
     * @throws IOException  when a connection cannot be properly set up.
     */
    public void connect(String address, int port, User user) throws IOException {
        createConnection(address, port);
        catchStreams();
        this.user = user;
        new Thread(new ServerListener(this, input)).start();
        registerConnection(user);
    }

    /**
     * @return the hostname of your connections host. Returns null when no connection is held.
     */
    public String getHostName() {

        if (connection != null)
            return connection.getInetAddress().getHostName();

        return null;
    }

    /**
     * Closes all connections to host. Recommended to use after
     * session finished.
     *
     * @throws IOException  when the connection cannot be properly
     *                      closed.
     */
    public void close() throws IOException {
        if (connection.isClosed()) {
            input.close();
            output.close();
            output.flush();
            connection.close();
        }
    }

    /**
     * @return whether the connection (socket) to host is closed or not.
     */
    public boolean isClosed() {
        return connection.isClosed();
    }

    /**
     * Adds a listener for the messages recieved from server.
     *
     * @param listener  the implementation of MessageListener
     *                  that wants to get a share of messages.
     */
    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a MessageListener from listenerslist. Making it
     * not recieve any further messages.
     *
     * @param listener  the implementation of MessageListener
     *                  to remove.
     */
    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sends the message recieved to all MessageListeners.
     * The message will first be formated into a more readable
     * standard than the irc-protocoled server's messages/responses.
     *
     * @param message   the message to be decoded and sent
     */
    public void messageRecieved(String message) {
        //TODO: Decode
        for (MessageListener listener : listeners)
            listener.messageRecieved(message);
    }

    /**
     * Sends an error-messagee to the listener as something
     * has gone wrong while reading a message.
     *
     * @param errorMessage  the error
     */
    public void errorOccured(String errorMessage) {
        for (MessageListener listener : listeners)
            listener.errorOccured(errorMessage);
    }

    /**
     * Sends a request of nickchange to server.
     *
     * @param user  the user to change name for
     * @param nick  the nick to change the user's name to
     * @throws  IllegalArgumentException when the server relpy
     *          indicates that the change of nickname cannot be made.
     *          For example when the nickname is invalid or already
     *          in use by another client on the server.
     */
    public void setNick(User user, String nick) throws IllegalArgumentException {

        //No reason to change the nickname if it is already correct
        if (user.getNickname().equals(nick))
            return;

        sendNick(nick);
        //TODO: Wait for response

        /*
            TODO:
            Send message to host. If response is good
            update user. Else throw exception.
         */
    }

    /**
     *
     * @param channel
     * @param mode
     */
    public void setChannelMode(String channel, Mode mode) {
        //Note that there is a maximum limit of three (3) changes per
        //command for modes that take a parameter.
    }

    //TODO: remove as you can do it with setChannelTopic?
    public void clearTopic(String channel) {
        setChannelTopic(channel, "");
    }

    /**
     *
     * @param channel
     * @param topic
     */
    public void setChannelTopic(String channel, String topic) {
        send("TOPIC " + channel + " :" + topic);
    }

    public String getChannelTopic(String channel) {

        send("TOPIC " + channel);

        //TODO: Wait for response ...

        return "";
    }

    public List<String> getChannelList() {


        //TODO: Wait for response
        return new ArrayList<>();
    }

    public void inviteUser(User user, String channel) {
        send("INVITE " + user + " " + channel);
    }

    /**
     * Sends a private message.
     * @param target    the channel or person to recieve the message.
     *                  if person in same server:
     *                  the target is his name on the network, else the name@server
     *                  ex.
     *                      jto@tolsun.oulu.fi
     * @param message   the message to be sent.
     */
    public void sendPrivateMessage(String target, String message) {
        send("PRIVMSG " + target + " :" + message);
    }

    public void sendPrivateMessage(String target, String message, String server) {

    }

    /**
     * Joins given channel.
     * TODO: Exception on unexcisting channel
     * @param channel
     */
    public void joinChannel(String channel) {
        send("JOIN " + channel);
    }

    /**
     * Leaves given channel(s).
     * @param channels   the channel(s) to leave
     */
    public void leaveChannel(String... channels) {
        String str = "";

        if (channels.length == 1)
            str = channels[0];
        else
            for (int i = 0; i < channels.length; ++i) {
                if (i == channels.length-1)
                    str += channels[i];
                else
                    str += channels[i] + ",";
            }

        send("PART " + str);
    }

    public void kickUser(User user, String channel) {

    }

    public void kickUser(User user, String channel, String reason) {

    }

    public void setUserMode(Mode mode) {

    }

    public void sendNotice() {

    }

    public void who() {

    }

    public void setAwayMessage() {

    }

    /**
     * Leaves the given channel(s).
     * @param leaveMessage  the message to be sent instead of the default one.
     * @param channels      the channel(s) to leave.
     */
    public void leaveChannel(String leaveMessage, String... channels) {
        String str = "";

        if (channels.length == 1)
            str = channels[0];
        else
            for (int i = 0; i < channels.length; ++i) {
                if (i == channels.length-1)
                    str += channels[i];
                else
                    str += channels[i] + ",";
            }

        send("PART " + str + " :" + leaveMessage);
    }

    /**
     * Leaves all channels the user is a member of.
     */
    public void leaveAllChannels() {
        send("JOIN " + 0);
    }

    public List getUsers(String channel) {
        return new ArrayList();
    }

    //TODO: Return whowas info?
    public void whowas() {

    }

    public void whois() {

    }

    /*
        PRIVATE METHODS.

        Probably not of interest for users of package.

     */

    /**
     * Catches I/O streams to host.
     *
     * @throws IOException  when streams cannot be setup.
     */
    private void catchStreams() throws IOException {
        output = new PrintWriter(connection.getOutputStream());
        output.flush();
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Establishes a connection via socket to host.
     *
     * @param address       the address where to find the host
     * @param port          the port where to connect to host
     * @throws IOException  when a connection cannot be created
     */
    private void createConnection(String address, int port) throws IOException {
        connection = new Socket(address, port);
    }

    /**
     * @return  a randomized password
     */
    private String generateConnectionPassword() {
        return UUID.randomUUID().toString();
    }

    /**
     * Response for server-ping.
     */
    private void pong(String target) {

    }

    private void quit() {

    }

    /**
     * Sends appropriate startup messages to the host.
     * According to standards in this order:
     *
     *      1. PASS <password>
     *      2. NICK <nickname>
     *      3. USER <nickname> <mode> <unused> :<realname>
     *
     * @param user      the user to connect to host
     */
    private void registerConnection(User user) {
        sendPass(generateConnectionPassword());
        sendNick(user.getNickname());
        sendUser(user);
    }

    /**
     * Sends message to host via writer.
     *
     * @param message   the message to be sent.
     */
    private void send(String message) {
        output.write(message + "\r\n");
        output.flush();
    }

    /**
     * Sends a NICK-message to the host.
     *
     * @param nickname  the name to be set as the
     *                  users at host.
     */
    private void sendNick(String nickname) {
        send("NICK " + nickname);
    }

    /**
     * Sends a PASS-message to the host to set
     * a connection-password.
     *
     * @param password  password to be set
     */
    private void sendPass(String password) {
        send("PASS " + password);
    }

    /**
     * Sends a USER-message to the host.
     *
     * @param user      the user to send user-info for.
     */
    private void sendUser(User user) {
        send("USER " + user.getNickname() + " " + user.getMode() + " * :" + user.getRealName());
    }
}
