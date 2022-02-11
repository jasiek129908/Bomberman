package com.janek.Client;

import com.janek.Client.Sprites.Blast;
import com.janek.Client.Sprites.Bomb;
import com.janek.Client.Sprites.Bomber;
import com.janek.Client.Sprites.BrickDestroyable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ServerConnection implements Runnable {

    private DataInputStream inputStream;
    private Bomberman bomberman;
    public static final Object lock = new Object();


    public ServerConnection(DataInputStream dataInputStream, Bomberman bomberman) {
        this.inputStream = dataInputStream;
        this.bomberman = bomberman;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = "";
                message = inputStream.readUTF();
                if (message.startsWith("brick")) {
                    destroyableBrick(message);
                } else if (message.startsWith("created")) {
                    initPlayer(message);
                } else if (message.startsWith("updatePosition")) {
                    updatePosition(message);
                } else if (message.startsWith("bomb")) {
                    newBomb(message);
                } else if (message.startsWith("explosion")) {
                    explosion(message);
                } else if (message.startsWith("blast")) {
                    newBlast(message);
                } else if (message.startsWith("endBlast")) {
                    endBlast(message);
                } else if (message.startsWith("clearBricks")) {
                    bomberman.brickDestroyable.clear();
                } else if (message.startsWith("death")) {
                    handleDeath(message);
                } else if (message.startsWith("chat")) {
                    String msg = message.substring(message.indexOf(" "));
                    bomberman.chatMessages.add(msg);
                } else if (message.startsWith("enemy")) {
                    newEnemy(message);
                } else if (message.startsWith("ready")) {
                    setPlayerReady(message);
                } else if (message.startsWith("start")) {
                    bomberman.gameStarted = true;
                } else if (message.startsWith("winner")) {
                    bomberman.player.isWinner = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setPlayerReady(String message) {
        String id = message.split(" ")[1];
        if (bomberman.player.id.equals(id)) {
            bomberman.player.ready = true;
        } else {
            Bomber bomber = bomberman.enemyPlayers.get(id);
            bomber.ready = true;
        }
    }

    private void newEnemy(String message) {
        String id = message.split(" ")[1];
        String x = message.split(" ")[2];
        String y = message.split(" ")[3];
        String nickName = message.split(" ")[4];
        if (bomberman.player != null && !bomberman.player.id.equals(id)) {
            if (!bomberman.enemyPlayers.containsKey(id)) {
                Bomber enemy = new Bomber(bomberman.playerSprite);
                enemy.nickName = nickName;
                enemy.setPosition(Float.valueOf(x), Float.valueOf(y));
                bomberman.enemyPlayers.put(id, enemy);
            }
        }
    }

    private void handleDeath(String message) {
        String bombermanId = message.split(" ")[1];
        if (bomberman.player.id.equals(bombermanId)) {
            bomberman.player.isDeath = true;
        } else {
            bomberman.enemyPlayers.remove(bombermanId);
        }
    }

    private void endBlast(String message) {
        String bombId = message.split(" ")[1];
        synchronized (lock) {
            Iterator<Map.Entry<String, Blast>> i = bomberman.blasts.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, Blast> next = i.next();
                int currBombId = next.getValue().bombId;
                if (currBombId == Integer.parseInt(bombId)) {
                    i.remove();
                }
            }
        }
    }

    private void newBlast(String message) {
        String blastId = message.split(" ")[1];
        String x = message.split(" ")[2];
        String y = message.split(" ")[3];
        String type = message.split(" ")[4];
        String bombId = message.split(" ")[5];

        float fx = Float.parseFloat(x);
        float fy = Float.parseFloat(y);
        Blast blast = null;

        if (type.equals("horizontal"))
            blast = new Blast(bomberman.blastHorizontalSprite, fx, fy, Integer.parseInt(bombId));
        else if (type.equals("vertical"))
            blast = new Blast(bomberman.blastVerticalSprite, fx, fy, Integer.parseInt(bombId));
        else if (type.equals("center"))
            blast = new Blast(bomberman.blastCenterSprite, fx, fy, Integer.parseInt(bombId));
        blast.setPosition(blast.x, blast.y);
        bomberman.blasts.put(blastId, blast);
    }

    private void explosion(String message) {
        String bombId = message.split(" ")[1];
        if (bomberman.bombs.get(bombId).bombermanId == Integer.valueOf(bomberman.player.id)) {
            bomberman.player.bombAvailable += 1;
        }
        bomberman.bombs.remove(bombId);
    }

    private void newBomb(String message) {
        String x = message.split(" ")[1];
        String y = message.split(" ")[2];
        String bombermanId = message.split(" ")[3];
        String bombId = message.split(" ")[4];
        Bomb bomb = new Bomb(bomberman.bombSprite, Integer.parseInt(bombId), Float.parseFloat(x), Float.parseFloat(y), Integer.parseInt(bombermanId));
        bomb.setPosition(bomb.x, bomb.y);
        bomberman.bombs.put(bombId, bomb);
    }

    private void updatePosition(String message) {
        String id = message.split(" ")[1];
        String x = message.split(" ")[2];
        String y = message.split(" ")[3];

        if (bomberman.enemyPlayers.get(id) != null) {
            Bomber bomber = bomberman.enemyPlayers.get(id);
            bomber.setX(Float.parseFloat(x));
            bomber.setY(Float.parseFloat(y));
            bomber.setPosition(bomber.getX(), bomber.getY());
            if (message.split(" ").length > 4) {
                bomber.direction = Bomberman.Direction.valueOf(message.split(" ")[4]);
                bomber.numberOfLastAnimationFrame = Integer.valueOf(message.split(" ")[5]);
                bomber.isMoving = Boolean.valueOf(message.split(" ")[6]);
                bomber.lastDirection = Bomberman.Direction.valueOf(message.split(" ")[7]);
                bomber.animationTimer = Float.valueOf(message.split(" ")[8]);

            }
        } else if (bomberman.player.id != null && bomberman.player.id.equals(id) && bomberman.player != null) {
            bomberman.player.x = Float.parseFloat(x);
            bomberman.player.y = Float.parseFloat(y);
            bomberman.player.setPosition(bomberman.player.x, bomberman.player.y);
        }
    }

    private void initPlayer(String message) {
        String x = message.split(" ")[2];
        String y = message.split(" ")[3];
        String nickName = message.split(" ")[4];
        bomberman.player.nickName = nickName;
        bomberman.player.id = message.split(" ")[1];
        bomberman.player.setPosition(Float.parseFloat(x), Float.parseFloat(y));
        bomberman.player.x = Float.parseFloat(x);
        bomberman.player.y = Float.parseFloat(y);
    }

    private void destroyableBrick(String message) {
        String number = message.split(" ")[1];
        String x = message.split(" ")[2];
        String y = message.split(" ")[3];
        BrickDestroyable brick = new BrickDestroyable(bomberman.brickDestroyableSprite, Float.parseFloat(x), Float.parseFloat(y));
        synchronized (lock) {
            bomberman.brickDestroyable.put(number, brick);
            bomberman.brickDestroyable.get(number).setPosition(brick.x, brick.y);
        }
    }
}
