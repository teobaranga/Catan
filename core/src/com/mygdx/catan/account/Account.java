package com.mygdx.catan.account;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Account otherAcc = (Account) obj;

        return username.equals(otherAcc.username);
    }
}
