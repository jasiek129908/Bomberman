package com.janek.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessNet;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket = null;
    public static final int PORT = 9999;

    private List<Player> clients;
    private Room room;
    public static int bombNumber = 0;
    public static int blastNumber = 0;

    public Server() throws IOException, InterruptedException {
        clients = new ArrayList<>();
        ServerSocketHints hints = new ServerSocketHints();
        hints.acceptTimeout = 0;
        serverSocket = new HeadlessNet(new HeadlessApplicationConfiguration()).newServerSocket(Net.Protocol.TCP, "localhost", PORT, hints);
        serverStart();
    }

    private void serverStart() throws IOException {
        int newPlayerId = 1;
        while (true) {
            if (room == null) {
                room = new Room();
            }
            Socket client = serverSocket.accept(null);

            Player player = new Player(newPlayerId++, 0, 0, client, room);
            clients.add(player);
            room.players.add(player);
            new Thread(new ClientHandler(room.players.get(newPlayerId-2))).start();

        }
    }
}
