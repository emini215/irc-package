package com.blixn.irc;

/**
 * Created by emini on 17/09/14.
 * Copyright and stuff, you know!
 *
 * Class to contain information of an irc-user.
 */
public class User {

    private String nickname;
    private String realName;
    private int mode;

    //TODO: Quitmessage

    private static final String NICKNAME_REGEX = "^[A-z{}|][A-z{}|0-9-]{0,8}$";

    /**
     * Creates a user-object required for usage with the package's client. Assuming the user
     * wants to stay visible.
     *
     * Guidelines for nickname limitations are taken from:
     * http://tools.ietf.org/html/rfc2812#section-3.1.5
     *
     * @param nickname      the desired nickname for the user. Must be 1-9 characters long.
     *                      Following the same BNF notation as ietf's nickname.
     *                      nickname   =  ( letter / special ) *8( letter / digit / special / "-" )
     *                      letter     =  A-Z / a-z
     *                      digit      =  0-9
     *                      special    =  "[", "]", "\", "`", "_", "^", "{", "|", "}"
     *
     * @param realName      the users real name. Can contain spaces. Must not be empty-string nor null.
     * @throws IllegalArgumentException whenever an argument does not match expectations.
     */
    public User(String nickname, String realName) throws IllegalArgumentException {
        this(nickname, realName, true);
    }

    /**
     * Creates a user-object required for usage with the package's client.
     *
     * Guidelines for nickname limitations are taken from:
     * http://tools.ietf.org/html/rfc2812#section-3.1.5
     *
     * @param nickname      the desired nickname for the user. Must be 1-9 characters long.
     *                      Following the same BNF notation as ietf's nickname.
     *                      nickname   =  ( letter / special ) *8( letter / digit / special / "-" )
     *                      letter     =  A-Z / a-z
     *                      digit      =  0-9
     *                      special    =  "[", "]", "\", "`", "_", "^", "{", "|", "}"
     *
     * @param realName      the users real name. Can contain spaces. Must not be empty-string nor null.
     * @param visible       if false the user will only be visible to other users in the same channel/channels.
     *                      Otherwise the user will be visible to everyone on the server.
     * @throws IllegalArgumentException whenever an argument does not match expectations.
     */
    public User(String nickname, String realName, boolean visible) throws IllegalArgumentException {

        if (nickname == null || !nickname.matches(NICKNAME_REGEX))
            throw new IllegalArgumentException("Username not allowed. Does not match irc-protocols nickname.");
        else
            this.nickname = nickname;

        if (realName == null || realName.equals(""))
            throw new IllegalArgumentException("Real name cannot be null or the empty string.");
        else
            this.realName = realName;

        mode = visible ? 0 : 8;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode == 0 || mode == 8)
            this.mode = mode;
    }


    @Override
    public String toString() {
        return this.nickname;
    }

    public static void main(String args[]) {
        new User("Blixn03", "Emil Nilsson");
        new User("Bli-x\\n", "derp");
        new User("{{Bli-xn", "derp");
        new User("Bli}-xn", "derp");
        new User("Bx[]]]n", "derp");
        new User("Bli^`-xn", "derp");
        try {
            new User("-Blixn", "derp");
            System.out.println("Ouch");
        } catch (IllegalArgumentException iae) {}
        try {
            new User("9Blixn", "derp");
            System.out.println("Ouch");
        } catch (IllegalArgumentException iae) {}
        try {
            new User("B/lixn", "derp");
            System.out.println("Ouch");
        } catch (IllegalArgumentException iae) {}
        try {
            new User("Bl&&ixn", "derp");
            System.out.println("Ouch");
        } catch (IllegalArgumentException iae) {}
        System.out.println("Done. No ouch's, huh? :)");
    }
}
