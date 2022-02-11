package com.janek.Client.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.janek.Client.Bomberman;
import com.janek.Client.Sprites.Bomber;

import java.io.IOException;
import java.util.Map;


public class LobbyScreen implements Screen {
    private Stage stage;
    private List<String> playersList;
    private Skin skin;
    private Label.LabelStyle style;
    private Bomberman bomberman;
    private SpriteBatch spriteBatch;
    private Label chatLabel;

    public LobbyScreen(Bomberman bomberman) {
        this.bomberman = bomberman;
        spriteBatch = new SpriteBatch();
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (bomberman.gameStarted) {
            bomberman.setScreen(new PlayScreen(bomberman));
        }

        spriteBatch.begin();
        String fullChat = "";
        for (String message : bomberman.chatMessages) {
            fullChat += message + "\n";
        }
        chatLabel.setText(fullChat);
        updatePlayersList();

        stage.draw();
        stage.act();
        spriteBatch.end();
    }

    @Override
    public void show() {
        style = new Label.LabelStyle();
        BitmapFont myFont = new BitmapFont();
        style.font = myFont;
        style.fontColor = Color.RED;

        skin = new Skin(Gdx.files.internal("items/uiskin.json"));
        stage = new Stage();
        Table root = new Table();
        root.setFillParent(true);

        Table playerInfoTable = buildPlayerInfoTable();
        Table chatTable = buildChatTable();

        final TextButton readyButton = new TextButton("Ready?", skin, "default");
        readyButton.setWidth(200f);
        readyButton.setHeight(20f);
        readyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    synchronized (bomberman.serverOut) {
                        bomberman.serverOut.writeUTF("ready " + bomberman.player.id);
                        readyButton.setText("Gotowy!");
                        readyButton.setDisabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Label roomLabel = new Label("Lobby ", skin, "default");
        roomLabel.setFontScale(3);
        root.add(roomLabel).colspan(3).padTop(-170);
        root.row();
        root.add(new Label("Players", skin, "default"));
        root.add(new Label(" ", skin, "default"));
        root.add(new Label("Chat", skin, "default"));
        root.row();
        root.add(playerInfoTable);
        root.add(new Label("    ", skin, "default"));
        root.add(chatTable);
        root.row();
        root.add(readyButton);

        stage.addActor(root);

        Gdx.input.setInputProcessor(stage);
    }

    private Table buildChatTable() {
        Table table = new Table();

        chatLabel = new Label("", skin);
        chatLabel.setWrap(true);
        chatLabel.setAlignment(Align.topLeft);

        ScrollPane chat_scroll = new ScrollPane(chatLabel, skin);
        chat_scroll.setFadeScrollBars(true);

        TextField message_field = new TextField("", skin);
        message_field.setMessageText("Press enter");
        message_field.setColor(0.2f, 0.4f, 0.3f, 1f);
        message_field.getStyle().fontColor = Color.WHITE;

        message_field.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER && !message_field.getText().isEmpty()) {
                    try {
                        synchronized (bomberman.serverOut) {
                            bomberman.serverOut.writeUTF("chat " + bomberman.player.id + " " + message_field.getText());
                        }
                        message_field.setText("Press enter");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });

        table.add(chat_scroll).width(220).height(220);
        table.row();
        table.add(message_field).width(220).height(20);

        return table;
    }

    private Table buildPlayerInfoTable() {
        Table table = new Table();
        playersList = new List<>(skin, "default");

        Label titles = new Label("   Players                     Status", skin, "default");
        titles.setColor(Color.WHITE);

        table.add(new ScrollPane(titles, skin)).align(Align.left).width(220).height(35);
        table.row();
        table.add(new ScrollPane(playersList, skin)).width(220).height(200);

        return table;
    }

    private void updatePlayersList() {
        Array<String> listToDisplay = new Array<>();
        listToDisplay.add(bomberman.player.nickName + " " + (bomberman.player.ready ? "ready" : "not ready"));
        for (Map.Entry<String, Bomber> player : bomberman.enemyPlayers.entrySet()) {
            listToDisplay.add(player.getValue().nickName + " " + (player.getValue().ready ? "ready" : "not ready"));
        }
        playersList.setItems(listToDisplay);
    }

    @Override
    public void resize(int i, int i1) {

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

    }
}
