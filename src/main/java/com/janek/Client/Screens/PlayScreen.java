package com.janek.Client.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.janek.Client.Bomberman;
import com.janek.Client.Sprites.Blast;
import com.janek.Client.Sprites.Bomb;
import com.janek.Client.Sprites.Bomber;
import com.janek.Client.Sprites.BrickDestroyable;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import static com.janek.Client.ServerConnection.lock;

public class PlayScreen implements Screen {
    private Bomberman bomberman;
    private OrthographicCamera gameCam;
    private SpriteBatch batch;
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private final float UPDATE_TIME = 1 / 30f;
    private final float ANIMATION_TIME = 1 / 7f;
    private float playerTimer = 0;

    public PlayScreen(Bomberman bomberman) {
        this.batch = new SpriteBatch();
        this.bomberman = bomberman;
    }

    @Override
    public void show() {
        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("maps/map.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(map, Bomberman.GAME_SCALE);
        this.gameCam = new OrthographicCamera();
        gameCam.position.set(360, 312, 0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handlePlayerDeath();
        handlePlayerWin();
        handleInput(delta);
        updateAndSendToServer(delta);

        renderer.setView(gameCam);
        renderer.render();
        batch.setProjectionMatrix(gameCam.combined);
        batch.begin();

        updateBomberAnimation(delta, bomberman.player);
        try {
            //bricksDestroyable
            synchronized (lock) {
                for (HashMap.Entry<String, BrickDestroyable> entry : bomberman.brickDestroyable.entrySet()) {
                    entry.getValue().draw(batch);
                }
            }
            //blasts
            synchronized (lock) {
                for (HashMap.Entry<String, Blast> entry : bomberman.blasts.entrySet()) {
                    blastHandler(entry.getValue(), delta);
                }
            }
            //player
            if (bomberman.player != null) {
                bomberman.player.draw(batch);
            }
            //enemyplayers
            for (Map.Entry<String, Bomber> entry : bomberman.enemyPlayers.entrySet()) {
                updateBomberAnimation(delta, entry.getValue());
                entry.getValue().draw(batch);
            }
            //bomb
            for (Map.Entry<String, Bomb> entry : bomberman.bombs.entrySet()) {
                bombHandler(entry.getValue(), delta);
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
        batch.end();
    }

    private void updateBomberAnimation(float dt, Bomber bomber) {
        if (bomber.animationTimer >= ANIMATION_TIME) {
            setBomberCurrentTexture(bomber);
            bomber.animationTimer = 0;
        }
        bomber.animationTimer += dt;
    }

    private void setBomberCurrentTexture(Bomber bomber) {
        switch (bomber.direction) {
            case UP:
                if (bomber.lastDirection == Bomberman.Direction.UP) {
                    if (bomber.numberOfLastAnimationFrame == 0) {
                        bomber.setRegion(bomberman.playerUpAnimationTexture1);
                        bomber.numberOfLastAnimationFrame = 1;
                    } else if (bomber.numberOfLastAnimationFrame == 1) {
                        bomber.setRegion(bomberman.playerUpAnimationTexture2);
                        bomber.numberOfLastAnimationFrame = 0;
                    }
                }
                if (!bomber.isMoving) {
                    bomber.setRegion(bomberman.playerUpTexture);
                }
                break;
            case DOWN:
                if (bomber.lastDirection == Bomberman.Direction.DOWN) {
                    if (bomber.numberOfLastAnimationFrame == 0) {
                        bomber.setRegion(bomberman.playerDownAnimationTexture1);
                        bomber.numberOfLastAnimationFrame = 1;
                    } else if (bomber.numberOfLastAnimationFrame == 1) {
                        bomber.setRegion(bomberman.playerDownAnimationTexture2);
                        bomber.numberOfLastAnimationFrame = 0;
                    }
                }
                if (!bomber.isMoving) {
                    bomber.setRegion(bomberman.playerDownTexture);
                }
                break;
            case LEFT:
                if (bomber.lastDirection == Bomberman.Direction.LEFT) {
                    if (bomber.numberOfLastAnimationFrame == 0) {
                        bomber.setRegion(bomberman.playerLeftAnimationTexture1);
                        bomber.numberOfLastAnimationFrame = 1;
                    } else if (bomber.numberOfLastAnimationFrame == 1) {
                        bomber.setRegion(bomberman.playerLeftAnimationTexture2);
                        bomber.numberOfLastAnimationFrame = 0;
                    }
                }
                if (!bomber.isMoving) {
                    bomber.setRegion(bomberman.playerLeftTexture);
                }
                break;
            case RIGHT:
                if (bomber.lastDirection == Bomberman.Direction.RIGHT) {
                    if (bomber.numberOfLastAnimationFrame == 0) {
                        bomber.setRegion(bomberman.playerRightAnimationTexture1);
                        bomber.numberOfLastAnimationFrame = 1;
                    } else if (bomber.numberOfLastAnimationFrame == 1) {
                        bomber.setRegion(bomberman.playerRightAnimationTexture2);
                        bomber.numberOfLastAnimationFrame = 0;
                    }
                }
                if (!bomber.isMoving) {
                    bomber.setRegion(bomberman.playerRightTexture);
                }
                break;
        }
    }

    public void handleInput(float dt) {
        int speed = 130;
        if (bomberman.player != null) {
            bomberman.player.lastDirection = bomberman.player.direction;
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                bomberman.player.y += speed * dt;
                bomberman.player.direction = Bomberman.Direction.UP;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                bomberman.player.y -= speed * dt;
                bomberman.player.direction = Bomberman.Direction.DOWN;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                bomberman.player.x -= speed * dt;
                bomberman.player.direction = Bomberman.Direction.LEFT;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                bomberman.player.x += speed * dt;
                bomberman.player.direction = Bomberman.Direction.RIGHT;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (bomberman.player.bombAvailable > 0) {
                    try {
                        sendBombPositionToServer();
                        bomberman.player.bombAvailable -= 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            bomberman.player.isMoving = Gdx.input.isKeyPressed(Input.Keys.ANY_KEY);
        }
    }

    private void sendBombPositionToServer() throws IOException {
        bomberman.serverOut.writeUTF("bomb " + bomberman.player.x + " " + bomberman.player.y + " " + bomberman.player.id);
        bomberman.serverOut.flush();
    }

    public void updateAndSendToServer(float dt) {
        playerTimer += dt;
        if (playerTimer >= UPDATE_TIME && bomberman.player != null) {
            try {
                String str = "position " + bomberman.player.x + " " + bomberman.player.y + " " +
                        bomberman.player.direction + " " + bomberman.player.numberOfLastAnimationFrame + " " + bomberman.player.isMoving
                        + " " + bomberman.player.lastDirection + " " + bomberman.player.animationTimer;
                bomberman.serverOut.writeUTF(str);
                bomberman.serverOut.flush();
                playerTimer = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void bombHandler(Bomb bomb, float dt) {
        if (bomb.timeElapsed > Bomb.TIME_TO_EXPLOSION) {
            try {
                if (bomb.bombermanId == Integer.valueOf(bomberman.player.id)) {
                    bomberman.serverOut.writeUTF("explosion " + bomb.x + " " + bomb.y + " " + bomb.id);
                    bomberman.serverOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bomb.draw(batch);
            bomb.timeElapsed += dt;
        }
    }

    private void blastHandler(Blast blast, float dt) {
        if (blast.timeElapsed > blast.BLAST_DURATION) {
            try {
                bomberman.serverOut.writeUTF("endBlast " + blast.bombId);
                bomberman.serverOut.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            blast.timeElapsed += dt;
            blast.draw(batch);
        }
    }

    private void handlePlayerDeath() {
        if (bomberman.player.isDeath) {
            GameOverScreen gameOverScreen = new GameOverScreen();
            bomberman.setScreen(gameOverScreen);
        }
    }

    private void handlePlayerWin() {
        if (bomberman.player.isWinner) {
            bomberman.setScreen(new WinScreen());
        }
    }

    @Override
    public void resize(int width, int height) {
        gameCam.viewportWidth = width;
        gameCam.viewportHeight = height;
        gameCam.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
    }


}
