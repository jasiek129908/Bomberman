package com.janek.Client.Sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Bomb extends Sprite {
    // in seconds
    public static final float TIME_TO_EXPLOSION = 3;
    public int id;
    public float x;
    public float y;
    public int bombermanId;
    public float timeElapsed;

    public Bomb(Sprite sprite, int id, float x, float y, int bombermanId) {
        super(sprite);
        this.id = id;
        this.x = x;
        this.y = y;
        this.bombermanId = bombermanId;
        this.timeElapsed = 0;
        this.setScale(0.8f,0.8f);
    }

    public void draw(SpriteBatch spriteBatch) {
        super.draw(spriteBatch);
    }
}
