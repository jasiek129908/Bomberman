package com.janek.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new Server();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
