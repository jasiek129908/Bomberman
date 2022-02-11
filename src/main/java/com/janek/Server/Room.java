package com.janek.Server;

import com.janek.Client.Bomberman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Room {
    public List<Player> players;
    //0=floor(player can move) 1=brickNotDestroyable 2=bricksToDestroy 3=blast
    public List<List<String>> map;
    public int brickNumber = 0;
    public boolean gameStarted = false;
    public static final int ROOM_SIZE = 2;

    public Room() {
        this.players = new ArrayList<Player>();
        this.map = new ArrayList<>();
        initializeMap();
        initializeDestroyableBricks();
    }

    private void initializeDestroyableBricks() {
        for (int i = 1; i < 12; i++) {
            List<String> row = map.get(i);
            if (i % 2 == 0) {
                for (int j = 1; j < 14; j++) {
                    if (j % 2 == 1)
                        row.set(j, "2");
                }
            } else {
                for (int j = 1; j < 14; j++) {
                    row.set(j, "2");
                }
            }
        }
        map.get(1).set(1, "0");
        map.get(1).set(2, "0");
        map.get(2).set(1, "0");
        map.get(1).set(12, "0");
        map.get(1).set(13, "0");
        map.get(2).set(13, "0");

        map.get(11).set(1, "0");
        map.get(11).set(2, "0");
        map.get(10).set(1, "0");
        map.get(11).set(12, "0");
        map.get(11).set(13, "0");
        map.get(10).set(13, "0");
    }

    private void initializeMap() {
        List<String> rowBorder, row1, row2, row3, row4, row5, row6, rowMix1, rowMix2, rowMix3, rowMix4, rowMix5;
        rowBorder = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            rowBorder.add("1");
        }

        row1 = createNormalRow();
        row2 = createNormalRow();
        row3 = createNormalRow();
        row4 = createNormalRow();
        row5 = createNormalRow();
        row6 = createNormalRow();
        rowMix1 = createMixRow();
        rowMix2 = createMixRow();
        rowMix3 = createMixRow();
        rowMix4 = createMixRow();
        rowMix5 = createMixRow();
        map.add(rowBorder);
        map.add(row1);
        map.add(rowMix1);
        map.add(row2);
        map.add(rowMix2);
        map.add(row3);
        map.add(rowMix3);
        map.add(row4);
        map.add(rowMix4);
        map.add(row5);
        map.add(rowMix5);
        map.add(row6);
        map.add(rowBorder);
    }

    private List<String> createNormalRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i == 0 || i == 14)
                row.add("1");
            else
                row.add("0");
        }
        return row;
    }

    private List<String> createMixRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i % 2 == 1) {
                row.add("0");
            } else {
                row.add("1");
            }
        }
        return row;
    }

    public void startGame() {
        players.get(0).x = 16 * 3;
        players.get(0).y = 16 * 3;

        if (players.size() > 1) {
            Player player2 = players.get(1);
            if (player2 != null) {
                player2.x = 48;
                player2.y = 11 * 48;
            }
        }
        if (players.size() > 2) {
            Player player3 = players.get(2);
            if (player3 != null) {
                player3.x = 13 * 48;
                player3.y = 11 * 48;
            }
        }
        if (players.size() > 3) {
            Player player4 = players.get(3);
            if (player4 != null) {
                player4.x = 13 * 48;
                player4.y = 48;
            }
        }
        gameStarted = true;
        sendBricksDestroyablePosition();
        for (Player player : players) {
            broadcast("updatePosition " + player.id + " " + player.x + " " + player.y);
        }
        broadcast("start");
    }

    public void broadcast(String msg) {
        for (Player player : players) {
            try {
                synchronized (player.outputStream) {
                    player.outputStream.writeUTF(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to broadcast to client.");
                players.remove(player);
            }
        }
    }

    //position(pixel per brick * scale) that is send is bottom left corner of a brick
    public void sendBricksDestroyablePosition() {
        for (int i = 1; i < map.size() - 1; i++) {
            for (int j = 1; j < map.get(0).size() - 1; j++) {
                if (map.get(i).get(j) == "2") {
                    float x = j * Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE;
                    float y = (map.size() - i - 1) * Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE;
                    broadcast("brick " + brickNumber++ + " " + x + " " + y);
                }
            }
        }
    }

    public void sendBlastPositions(String bombId) {
        for (int i = 1; i < map.size() - 1; i++) {
            for (int j = 1; j < map.get(0).size() - 1; j++) {
                if (map.get(i).get(j).startsWith("3")) {
                    float x = j * Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE;
                    float y = (map.size() - i - 1) * Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE;
                    String type = "";
                    String brick = map.get(i).get(j);
                    if (brick.endsWith("left") || brick.endsWith("right")) type = "horizontal";
                    else if (brick.endsWith("up") || brick.endsWith("down")) type = "vertical";
                    else if (brick.endsWith("center")) type = "center";
                    broadcast("blast " + Server.blastNumber++ + " " + x + " " + y + " " + type + " " + bombId);
                }
            }
        }
    }

    public void clearBlast(String bombId) {
        for (int i = 1; i < map.size() - 1; i++) {
            for (int j = 1; j < map.get(0).size() - 1; j++) {
                if (map.get(i).get(j).length() > 1) {
                    String brick = map.get(i).get(j);
                    if(brick.split(" ").length>1) {
                        String bombIdOfBrick = brick.split(" ")[1];
                        if (bombIdOfBrick.equals(bombId)) {
                            map.get(i).set(j, "0");
                        }
                    }
                }
            }
        }
    }

    public void showMap() {
        for (int i = 0; i < map.size(); i++) {
            System.out.println(map.get(i));
        }
    }
}
