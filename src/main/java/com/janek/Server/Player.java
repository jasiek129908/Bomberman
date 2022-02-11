package com.janek.Server;

import com.badlogic.gdx.net.Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Player {
    public int id;
    public float x;
    public float y;
    public int bombAvailable;
    public boolean isDeath;
    public boolean isWinner;
    public boolean ready;
    public String nickName;
    public String direction;
    public String animationFrame;
    public String isMoving;
    public String lastDirection;
    public String animationTimer;

    public Socket socket;
    public DataInputStream inputStream;
    public DataOutputStream outputStream;

    public Room room;

    public Player(int id, float x, float y, Socket socket, Room room) throws IOException {
        this.id = id;
        this.x = x;
        this.y = y;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.room = room;
        this.ready = false;
        this.direction = "DOWN";
        this.animationFrame = "0";
        this.isMoving = "false";
        this.lastDirection = "DOWN";
        this.animationTimer = "0";
        this.bombAvailable = 2;
        this.isDeath = false;
        this.isWinner = false;
    }
}
