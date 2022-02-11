package com.janek.Client.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.janek.Client.Bomberman;

import java.io.IOException;

public class WelcomeScreen implements Screen {
    private Stage stage;
    private Sprite backgroundSprite;
    private SpriteBatch spriteBatch;
    private TextField textFieldNick;
    private Bomberman bomberman;

    public WelcomeScreen(Bomberman bomberman) {
        this.bomberman = bomberman;
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        Skin skin = new Skin(Gdx.files.internal("items/uiskin.json"));
        Label.LabelStyle label1Style = new Label.LabelStyle();
        BitmapFont myFont = new BitmapFont();
        label1Style.font = myFont;
        label1Style.fontColor = Color.RED;
        stage = new Stage();

        textFieldNick = new TextField("", skin);
        textFieldNick.setMessageText("Enter nick");
        textFieldNick.setColor(0.2f, 0.4f, 0.3f, 1f);
        textFieldNick.getStyle().fontColor = Color.WHITE;
        textFieldNick.setPosition(Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2);

        final TextButton readyButton = new TextButton("Confirm", skin, "default");
        readyButton.setWidth(200f);
        readyButton.setHeight(20f);
        readyButton.setPosition(Gdx.graphics.getWidth() / 2 - 130, Gdx.graphics.getHeight() / 2 - 100);
        readyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    synchronized (bomberman.serverOut) {
                        bomberman.player.nickName = textFieldNick.getText();
                        bomberman.serverOut.writeUTF("nickName " + textFieldNick.getText());
                        bomberman.serverOut.flush();
                        bomberman.serverOut.writeUTF("connected");
                        bomberman.setScreen(new LobbyScreen(bomberman));
                        bomberman.serverOut.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Texture backgroundTexture = new Texture("items/background.png");
        backgroundSprite = new Sprite(backgroundTexture);
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        stage.addActor(textFieldNick);
        stage.addActor(readyButton);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        backgroundSprite.draw(spriteBatch);
        spriteBatch.end();

        stage.draw();
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
