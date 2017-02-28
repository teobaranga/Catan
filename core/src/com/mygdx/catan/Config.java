package com.mygdx.catan;

public class Config {
    /** The IP of the server. All clients need to connect to this IP. */
    public static final String IP = "localhost";

    /** TCP port used when connecting to the server */
    public static final int TCP = 54555;

    /** UDP port used when connecting to the server */
    public static final int UDP = 54777;

    /** Maximum number of players in a game */
    public static final int MAX_PLAYERS = 5;

    /** Path of the file where the current account is stored */
    public static final String ACCOUNT_PATH = "acct.bin";
}
