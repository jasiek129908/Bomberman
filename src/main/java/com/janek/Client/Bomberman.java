package com.janek.Client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.janek.Client.Screens.WelcomeScreen;
import com.janek.Client.Sprites.Blast;
import com.janek.Client.Sprites.Bomb;
import com.janek.Client.Sprites.Bomber;
import com.janek.Client.Sprites.BrickDestroyable;
import com.janek.Server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bomberman extends Game {
    //game variables
    public enum Direction {UP, DOWN, LEFT, RIGHT}

    public static final int GAME_SCALE = 3;
    public static final int BRICK_SIZE = 16;
    public boolean gameStarted = false;

    //player variables
    public Bomber player;

    //server variables
    public Socket serverSocket;
    public DataOutputStream serverOut;
    public DataInputStream serverIn;

    //chat messages
    public List<String> chatMessages;

    //map variables
    public Map<String, BrickDestroyable> brickDestroyable;
    public Map<String, Bomber> enemyPlayers;
    public Map<String, Bomb> bombs;
    public Map<String, Blast> blasts;

    //textures variables
    public Texture everythingTexture;
    public Texture playersTexture;
    public Sprite brickDestroyableSprite;
    public Sprite playerSprite;
    public TextureRegion playerUpTexture;
    public TextureRegion playerUpAnimationTexture1;
    public TextureRegion playerUpAnimationTexture2;
    public TextureRegion playerDownTexture;
    public TextureRegion playerDownAnimationTexture1;
    public TextureRegion playerDownAnimationTexture2;
    public TextureRegion playerLeftTexture;
    public TextureRegion playerLeftAnimationTexture1;
    public TextureRegion playerLeftAnimationTexture2;
    public TextureRegion playerRightTexture;
    public TextureRegion playerRightAnimationTexture1;
    public TextureRegion playerRightAnimationTexture2;
    public Sprite bombSprite;
    public Sprite blastCenterSprite;
    public Sprite blastHorizontalSprite;
    public Sprite blastVerticalSprite;

    @Override
    public void create() {
        brickDestroyable = new HashMap<>();
        enemyPlayers = new HashMap<>();
        bombs = new HashMap<>();
        blasts = new HashMap<>();
        chatMessages = new ArrayList<>();

        initializeSpritesAndTextures();
        connectToServer();
        WelcomeScreen lobbyScreen = new WelcomeScreen(this);
        setScreen(lobbyScreen);
        player= new Bomber(playerSprite);
    }

    private void connectToServer() {
        serverSocket = Gdx.net.newClientSocket(Protocol.TCP,"localhost", Server.PORT,new SocketHints());
        serverOut = new DataOutputStream(serverSocket.getOutputStream());
        serverIn = new DataInputStream(serverSocket.getInputStream());
        new Thread(new ServerConnection(serverIn, this)).start();
    }

    @Override
    public void render() {
        super.render();
    }

    private void initializeSpritesAndTextures() {
        everythingTexture = new Texture("items/everything.png");
        playersTexture = new Texture("players/players.png");

        brickDestroyableSprite = new Sprite(new TextureRegion(everythingTexture, 16 * 4, 16 * 3, 16, 16));
        brickDestroyableSprite.setSize(16f * GAME_SCALE, 16f * GAME_SCALE);

        playerDownTexture = new TextureRegion(playersTexture, 0, 0, 33, 33);
        playerSprite = new Sprite(new TextureRegion(playersTexture, 0, 0, 33, 33));
        playerSprite.setSize(16 * GAME_SCALE, 16 * GAME_SCALE);

        playerUpTexture = new TextureRegion(playersTexture, 6 * 33, 0, 33, 33);
        playerLeftTexture = new TextureRegion(playersTexture, 3 * 33, 0, 33, 33);
        playerLeftTexture.flip(true, false);
        playerRightTexture = new TextureRegion(playersTexture, 3 * 33, 0, 33, 33);

        bombSprite = new Sprite(new TextureRegion(everythingTexture, 0, 16 * 3, 16, 16));
        bombSprite.setSize(16 * GAME_SCALE, 16 * GAME_SCALE);

        blastCenterSprite = new Sprite(new TextureRegion(everythingTexture, 16 * 2, 16 * 6, 16, 16));
        blastCenterSprite.setSize(16 * GAME_SCALE, 16 * GAME_SCALE);

        blastVerticalSprite = new Sprite(new TextureRegion(everythingTexture, 16 * 2, 16 * 5, 16, 16));
        blastVerticalSprite.setSize(16 * GAME_SCALE, 16 * GAME_SCALE);

        blastHorizontalSprite = new Sprite(new TextureRegion(everythingTexture, 16, 16 * 6, 16, 16));
        blastHorizontalSprite.setSize(16 * GAME_SCALE, 16 * GAME_SCALE);

        playerUpAnimationTexture1 = new TextureRegion(playersTexture, 33 * 7, 0, 33, 33);
        playerUpAnimationTexture2 = new TextureRegion(playersTexture, 33 * 8, 0, 33, 33);

        playerDownAnimationTexture1 = new TextureRegion(playersTexture, 33 * 1, 0, 33, 33);
        playerDownAnimationTexture2 = new TextureRegion(playersTexture, 33 * 2, 0, 33, 33);

        playerRightAnimationTexture1 = new TextureRegion(playersTexture, 33 * 4, 0, 33, 33);
        playerRightAnimationTexture2 = new TextureRegion(playersTexture, 33 * 5, 0, 33, 33);

        playerLeftAnimationTexture1 = new TextureRegion(playersTexture, 33 * 4, 0, 33, 33);
        playerLeftAnimationTexture1.flip(true, false);
        playerLeftAnimationTexture2 = new TextureRegion(playersTexture, 33 * 5, 0, 33, 33);
        playerLeftAnimationTexture2.flip(true, false);

    }
}
