package com.janek.Server;

import com.janek.Client.Bomberman;
import java.io.IOException;

import static com.janek.Server.Room.ROOM_SIZE;

public class ClientHandler implements Runnable {
    private Player player;
    private float closeUp = 15f;

    public ClientHandler(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = player.inputStream.readUTF();
                if (message.equalsIgnoreCase("connected")) {
                    newConnection();
                } else if (message.startsWith("position")) {
                    positionChanged(message);
                } else if (message.startsWith("bomb")) {
                    registerBomb(message);
                } else if (message.startsWith("explosion")) {
                    handleBombExplosion(message);
                } else if (message.startsWith("endBlast")) {
                    endBlast(message);
                } else if (message.startsWith("nickName")) {
                    nameUser(message);
                } else if (message.startsWith("chat")) {
                    broadCastMessage(message);
                } else if (message.startsWith("ready")) {
                    setUserReadyAndCheckGameStart(message);
                }
                player.outputStream.flush();
            } catch (IOException e) {
                System.out.println("client thread");
                e.printStackTrace();
                return;
            }
        }
    }

    private void setUserReadyAndCheckGameStart(String message) {
        String boomberId = message.split(" ")[1];
        Player player = this.player.room.players.stream()
                .filter(player1 -> player1.id == Integer.valueOf(boomberId))
                .findFirst().get();
        player.ready = true;
        if (player.room.players.stream().filter(pla -> pla.ready == false).count() == 0 && player.room.players.size() == ROOM_SIZE) {
            player.room.startGame();
        } else {
            player.room.broadcast("ready " + player.id);
        }
    }

    private void broadCastMessage(String message) {
        String boomberId = message.split(" ")[1];
        String chatMessage = message.split(" ")[2];
        String nick = player.room.players.stream()
                .filter(player -> player.id == Integer.valueOf(boomberId))
                .findFirst()
                .get().nickName;
        player.room.broadcast("chat " + nick + " " + chatMessage);
    }

    private void nameUser(String message) {
        String nickName = message.split(" ")[1];
        player.nickName = nickName;
    }

    private void endBlast(String message) {
        player.room.broadcast(message);
        player.room.broadcast("clearBricks");
        player.room.clearBlast(message.split(" ")[1]);
        player.room.sendBricksDestroyablePosition();
    }

    private void handleBombExplosion(String message) {
        Room room = player.room;
        String bombId = message.split(" ")[3];
        room.broadcast("explosion " + bombId);

        String x = message.split(" ")[1];
        String y = message.split(" ")[2];
        int mapX = getXMapForBlast(x);
        int mapY = getYMapForBlast(y);

        room.map.get(mapY).set(mapX, "3 " + bombId + " center");
        //0=floor(player can move) 1=brickNotDestroyable 2=bricksToDestroy
        int count = 0;
        while (!room.map.get(mapY).get(mapX - 1 - count).equals("1") && count < 2) {
            if (room.map.get(mapY).get(mapX - 1 - count).equals("2")) {
                room.map.get(mapY).set(mapX - 1 - count, "3 " + bombId + " +left");
                break;
            }
            room.map.get(mapY).set(mapX - 1 - count, "3 " + bombId + " -left");
            count++;
        }
        //right
        count = 0;
        while (!room.map.get(mapY).get(mapX + 1 + count).equals("1") && count < 2) {
            if (room.map.get(mapY).get(mapX + 1 + count).equals("2")) {
                room.map.get(mapY).set(mapX + 1 + count, "3 " + bombId + " +right");
                break;
            }
            room.map.get(mapY).set(mapX + 1 + count, "3 " + bombId + " -right");
            count++;
        }
        //up
        for (count = 0; count < 2; count++) {
            if (mapY - 1 - count >= 0) {
                if (!room.map.get(mapY - 1 - count).get(mapX).equals("1")) {
                    if (room.map.get(mapY - 1 - count).get(mapX).equals("2")) {
                        room.map.get(mapY - 1 - count).set(mapX, "3 " + bombId + " +up");
                        break;
                    }
                    room.map.get(mapY - 1 - count).set(mapX, "3 " + bombId + " -up");
                } else {
                    break;
                }
            }
        }
        //down
        for (count = 0; count < 2; count++) {
            if (mapY + 1 + count < room.map.size()) {
                if (!room.map.get(mapY + 1 + count).get(mapX).equals("1")) {
                    if (room.map.get(mapY + 1 + count).get(mapX).equals("2")) {
                        room.map.get(mapY + 1 + count).set(mapX, "3 " + bombId + " +down");
                        break;
                    }
                    room.map.get(mapY + 1 + count).set(mapX, "3 " + bombId + " -down");
                } else {
                    break;
                }
            }
        }
        room.sendBlastPositions(bombId);
    }

    private void registerBomb(String message) {
        player.room.broadcast(message + " " + Server.bombNumber++);
    }

    private void positionChanged(String message) throws IOException {
        String x = message.split(" ")[1];
        String y = message.split(" ")[2];
        String direction = message.split(" ")[3];
        String animationFrame = message.split(" ")[4];
        String isMoving = message.split(" ")[5];
        String lastDirection = message.split(" ")[6];
        String animationTimer = message.split(" ")[7];
        player.direction = direction;
        player.animationFrame = animationFrame;
        player.isMoving = isMoving;
        player.lastDirection = lastDirection;
        player.animationTimer = animationTimer;
        if (Float.valueOf(x) < 0)
            return;
        switch (direction) {
            case "UP":
                if (Math.abs(getYMap(y) - getYMap(String.valueOf(player.y))) == 1
                        && !player.room.map.get(getYMap(y) - 1).get(getXMap(String.valueOf(player.x))).equals("0")
                        && Math.abs(player.y - Float.valueOf(y)) < closeUp
                        && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) != 0) {
                    player.y = (player.room.map.size() - getYMap(y) - 1) * Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE;
                }
                if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) == 0) {
                    if (player.room.map.get(getYMap(y) - 1).get(getXMap(String.valueOf(player.x))).equals("0")) {
                        player.y = Float.valueOf(y);
                    }
                } else if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 36 && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 48) {
                    if (player.room.map.get(getYMap(y) - 1).get(getXMap(String.valueOf(player.x)) + 1).equals("0")) {
                        player.y = Float.valueOf(y);
                        int temp = (int) (Float.valueOf(player.x) / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * (temp + 1);
                    }
                } else if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 0 && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 12) {
                    if (player.room.map.get(getYMap(y) - 1).get(getXMap(String.valueOf(player.x))).equals("0")) {
                        player.y = Float.valueOf(y);
                        int temp = (int) (Float.valueOf(player.x) / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * temp;
                    }
                }
                break;
            case "DOWN":
                if (Math.abs(getYMap(y) - getYMap(String.valueOf(player.y))) == 1
                        && !player.room.map.get(getYMap(y)).get(getXMap(String.valueOf(player.x))).equals("0")
                        && Math.abs(player.y - Float.valueOf(y)) < closeUp
                        && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) != 0) {
                    player.y = (player.room.map.size() - getYMap(y)) * Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE;
                }
                if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) == 0) {
                    if (player.room.map.get(getYMap(y)).get(getXMap(String.valueOf(player.x))).equals("0")) {
                        player.y = Float.valueOf(y);
                    }
                } else if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 36 && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 48) {
                    if (player.room.map.get(getYMap(y)).get(getXMap(String.valueOf(player.x)) + 1).equals("0")) {
                        player.y = Float.valueOf(y);
                        int temp = (int) (player.x / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * (temp + 1);
                    }
                } else if (player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 0 && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 12) {
                    if (player.room.map.get(getYMap(y)).get(getXMap(String.valueOf(player.x))).equals("0")) {
                        player.y = Float.valueOf(y);
                        int temp = (int) (player.x / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * temp;
                    }
                }
                break;
            case "LEFT":
                if (Math.abs(getXMap(x) - getXMap(String.valueOf(player.x))) == 1
                        && !player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x)).equals("0")
                        && Math.abs(player.x - Float.valueOf(x)) < closeUp
                        && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) != 0) {
                    int temp = (int) (player.x / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                    if (temp == 0) temp++;
                    player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * temp;
                }
                if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) == 0) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x)).equals("0")) {
                        player.x = Float.valueOf(x);
                    }
                } else if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 30 && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 48) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y)) + 1).get(getXMap(x) - 1).equals("0")) {
                        player.x = Float.valueOf(x);
                        int temp = (int) (player.y / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.y = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * (temp + 1);
                    }
                } else if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 0 && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 12) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x) - 1).equals("0")) {
                        player.x = Float.valueOf(x);
                        int temp = (int) (player.y / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.y = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * temp;
                    }
                }
                break;
            case "RIGHT":
                if (Math.abs(getXMap(x) - getXMap(String.valueOf(player.x))) == 1
                        && !player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x) + 1).equals("0")
                        && Math.abs(player.x - Float.valueOf(x)) < closeUp
                        && player.x % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) != 0) {
                    int temp = (int) (player.x / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                    if (temp == 0) temp++;
                    player.x = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * (temp + 1);
                }
                if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) == 0) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x) + 1).equals("0")) {
                        player.x = Float.valueOf(x);
                    }
                } else if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 30 && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 48) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y)) + 1).get(getXMap(x) + 1).equals("0")) {
                        player.x = Float.valueOf(x);
                        int temp = (int) (player.y / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        player.y = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * (temp + 1);
                    }
                } else if (player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) > 0 && player.y % (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE) <= 12) {
                    if (player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(x) + 1).equals("0")) {
                        player.x = Float.valueOf(x);
                        int temp = (int) (player.y / (Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE));
                        if (temp == 0) temp++;
                        if (temp == 0) temp++;
                        player.y = Bomberman.BRICK_SIZE * Bomberman.GAME_SCALE * temp;
                    }
                }
                break;
            default:
                break;
        }
        handleDeath(x, y);
        for (Player gamer : player.room.players) {
            String str = "updatePosition " + gamer.id + " " + gamer.x + " " + gamer.y + " " + gamer.direction + " " +
                    gamer.animationFrame + " " + gamer.isMoving + " " + gamer.lastDirection + " " + gamer.animationTimer;
            player.outputStream.writeUTF(str);
        }
    }

    private void handleDeath(String x, String y) {
        if (player.room.map.get(getYMapForBlast(y)).get(getXMapForBlast(x)).startsWith("3")
                || player.room.map.get(getYMap(String.valueOf(player.y))).get(getXMap(String.valueOf(player.x))).startsWith("3")
                || player.room.map.get(getYMap(y)).get(getXMap(x)).startsWith("3")) {
            player.room.broadcast("death " + player.id);
            player.room.players.remove(player);
            if (player.room.players.size() == 1) {
                player.room.broadcast("winner");
            }
        }
    }

    private void newConnection() throws IOException {
        player.outputStream.writeUTF("created " + player.id + " " + player.x + " " + player.y + " " + player.nickName);
        for (Player player2 : player.room.players) {
            if (player2.nickName != null)
                player.room.broadcast("enemy " + player2.id + " " + player2.x + " " + player2.y + " " + player2.nickName);
        }
    }

    private int getXMap(String x) {
        float temp = Float.parseFloat(x);
        int column = (int) (temp / (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        return column;
    }

    private int getYMap(String y) {
        float temp = Float.parseFloat(y);
        int mapSize = player.room.map.size();
        int row = (int) (temp / (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        return mapSize - 1 - row;
    }

    private int getXMapForBlast(String x) {
        float temp = Float.parseFloat(x);
        int column = (int) (temp / (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        float remainder = temp - (column * (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        if (remainder >= (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE) / 2) {
            column += 1;
        }
        return column;
    }

    private int getYMapForBlast(String y) {
        float temp = Float.parseFloat(y);
        int mapSize = player.room.map.size();
        int row = (int) (temp / (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        float remainder = temp - (row * (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE));
        if (remainder >= (Bomberman.GAME_SCALE * Bomberman.BRICK_SIZE) / 2) {
            row += 1;
        }
        return mapSize - 1 - row;
    }
}
