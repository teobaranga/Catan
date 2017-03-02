package com.mygdx.catan.account;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mygdx.catan.CatanGame;

import java.io.*;
import java.nio.file.Files;

import static com.mygdx.catan.Config.ACCOUNT_PATH;

public class AccountManager {

    /**
     * Cache the account used when logging in.
     *
     * @param account the account to be written to disk
     * @return true if successful, false otherwise
     */
    public static boolean writeLocalAccount(Account account) {
        try {
            final Kryo kryo = CatanGame.client.getKryo();
            final Output output = new Output(new FileOutputStream(ACCOUNT_PATH));
            kryo.writeObject(output, account);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve the account that was previously used to log in
     *
     * @return the local account if it exists, null otherwise
     */
    public static Account getLocalAccount() {
        try {
            final Kryo kryo = CatanGame.client.getKryo();
            final Input input = new Input(new FileInputStream(ACCOUNT_PATH));
            final Account account = kryo.readObject(input, Account.class);
            input.close();
            return account;
        } catch (FileNotFoundException e) {
            System.out.println("Cached account not found");
            return null;
        }
    }

    /**
     * Delete the cached account, if it exists
     */
    public static void deleteLocalAccount() {
        File file = new File(ACCOUNT_PATH);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
