package com.mygdx.catan;

public class Account {

    private String username;
    private String password;

    /**
     * Create a new account.
     * Empty constructor needs to be present for serialization to work.
     */
    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
