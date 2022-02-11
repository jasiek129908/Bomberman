package com.janek.Client.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

public class WinScreen implements Screen {
    private Stage stage;
    private SpriteBatch spriteBatch;

    public WinScreen() {
        spriteBatch = new SpriteBatch();
    }
    @Override
    public void show() {
        Label.LabelStyle label1Style = new Label.LabelStyle();
        BitmapFont myFont = new BitmapFont();
        label1Style.font = myFont;
        label1Style.fontColor = Color.RED;
        stage = new Stage();

        Label label1 = new Label("YOU WON!", label1Style);
        label1.setPosition(Gdx.graphics.getWidth() /2 - 65f, Gdx.graphics.getHeight()/2 - 10f);
        label1.setAlignment(Align.center);
        label1.setFontScale(4.5f);
        stage.addActor(label1);
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();
        stage.draw();
        spriteBatch.end();
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
